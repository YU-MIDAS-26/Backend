package com.bsight.springserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record RegisterStepTwoRequest(

        @NotBlank(message = "아이디를 입력해 주세요.")
        String studentId,

        @NotBlank(message = "사업자 등록 번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{10}$", message = "사업자 등록 번호는 하이픈 없이 10자리 숫자로 입력해 주세요.")
        String businessRegistrationNumber,

        @NotBlank(message = "회사명을 입력해 주세요.")
        String companyName,

        @NotBlank(message = "대표자명을 입력해 주세요.")
        String representativeName,

        @NotBlank(message = "대표번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9]{9,11}$", message = "대표번호는 하이픈 없이 9~11자리 숫자로 입력해 주세요.")
        String representativePhone,

        @NotBlank(message = "회사 주소를 입력해 주세요.")
        String companyAddress,

        @NotBlank(message = "기업 구분을 선택해 주세요.")
        String businessType,

        @NotNull(message = "개업일을 입력해 주세요.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate openingDate,

        @NotBlank(message = "과세 구분을 선택해 주세요.")
        String taxType,

        @NotBlank(message = "업태명을 입력해 주세요.")
        String businessCategory,

        @NotBlank(message = "종목명을 입력해 주세요.")
        String businessItem,

        @NotNull(message = "사업자등록증 파일을 첨부해 주세요.")
        MultipartFile businessLicenseFile
) {
}