package com.bsight.springserver.global.security.jwt;

import com.bsight.springserver.domain.auth.repository.JwtBlacklistTokenRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.response.ApiResponse;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import com.bsight.springserver.global.security.auth.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",

            "/api/auth/email/request",
            "/api/auth/email/verify",
            "/api/auth/student-id/check",
            "/api/auth/register/step-one",
            "/api/auth/register/step-two",
            "/api/auth/login",
            "/api/auth/password-reset/request",
            "/api/auth/password-reset/confirm"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtBlacklistTokenRepository jwtBlacklistTokenRepository;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null) {
                if (jwtBlacklistTokenRepository.existsByToken(token)) {
                    throw new CustomException(ErrorCode.ALREADY_LOGGED_OUT);
                }

                jwtTokenProvider.validateToken(token);

                Long userId = jwtTokenProvider.getUserId(token);
                CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            SecurityContextHolder.clearContext();
            writeErrorResponse(response, e.getErrorCode().getHttpStatus().value(), e.getErrorCode().getMessage());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String body = objectMapper.writeValueAsString(ApiResponse.error(message));
        response.getWriter().write(body);
    }
}