package com.zinphraek.leprestigehall.utilities.helpers;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

import static com.zinphraek.leprestigehall.domain.constants.Regex.LOWERCASE_LETTERS_AND_UNDERSCORE_REGEX;

public class GenericHelper {

  /**
   * Construct a file name with extension
   *
   * @param baseName  - The base name to be used
   * @param mediaFile - The file content.
   * @return - The constructed string.
   */
  public static String generateFileName(String baseName, MultipartFile mediaFile) {

    StringBuilder filename = new StringBuilder();
    filename.append(baseName.strip().replaceAll(" ", "_").toLowerCase());
    if (mediaFile != null && mediaFile.getOriginalFilename() != null) {
      String[] filenameParts = Objects.requireNonNull(mediaFile.getOriginalFilename()).split("\\.");
      if (filenameParts.length > 1) {
        String extension = filenameParts[filenameParts.length - 1].toLowerCase();
        filename.append(".").append(extension);
      } else {
        String[] filePart = Objects.requireNonNull(mediaFile.getContentType()).split("/");
        if (filePart.length > 1) {
          String extension = filePart[filePart.length - 1].toLowerCase();
          filename.append(".").append(extension);
        }
      }
    }
    return filename.toString();
  }

  /**
   * Validates a file name
   *
   * @param fileName - The file name to be validated
   */
  public static void validateFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "File name must not be null or blank");
    }

    if (!fileName.matches(LOWERCASE_LETTERS_AND_UNDERSCORE_REGEX)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "File name must be at least 3 characters long and should only contain lower case letters");
    }
  }


  /**
   * Creates a CustomPage object from the request parameters
   *
   * @param params - The request parameters
   * @return - The CustomPage object
   */
  public static CustomPage createCustomPageFromParams(Map<String, String> params) {
    CustomPage customPage = new CustomPage();

    String page = params.get("page");
    String pageSize = params.get("pageSize");
    String sortOrder = params.get("sortOrder");
    String sortField = params.get("sortField");

    if (StringUtils.isNumeric(page)) {
      customPage.setPageNumber(Integer.parseInt(page));
    }

    if ("all".equalsIgnoreCase(pageSize)) {
      customPage.setPageSize(Integer.MAX_VALUE);
    } else if (StringUtils.isNumeric(pageSize)) {
      customPage.setPageSize(Integer.parseInt(pageSize));
    }

    if (sortOrder != null) {
      customPage.setSortDirection("ASC".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC);
    }

    if (sortField != null) {
      customPage.setSortBy(sortField);
    }

    return customPage;
  }

  /**
   * Build a Pageable object from a CustomPage object.
   *
   * @param customPage - The CustomPage object.
   * @return - The Pageable object.
   */
  public static Pageable buildPageRequestFromCustomPage(CustomPage customPage) {
    return PageRequest.of(
        customPage.getPageNumber(),
        customPage.getPageSize(),
        customPage.getSortDirection(),
        customPage.getSortBy());
  }
}
