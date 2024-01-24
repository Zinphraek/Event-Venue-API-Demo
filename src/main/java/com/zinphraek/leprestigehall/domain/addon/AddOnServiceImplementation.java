package com.zinphraek.leprestigehall.domain.addon;

import com.zinphraek.leprestigehall.domain.blobstorage.BlobStorageService;
import com.zinphraek.leprestigehall.domain.media.AddOnMedia;
import com.zinphraek.leprestigehall.domain.media.MediaService;
import com.zinphraek.leprestigehall.utilities.helpers.CustomMultipartFile;
import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class AddOnServiceImplementation implements AddOnService {

  private final Logger logger = LogManager.getLogger(AddOnServiceImplementation.class);

  @Autowired
  private final AddOnRepository addOnRepository;

  @Autowired
  private final MediaService mediaService;

  @Autowired
  private final BlobStorageService blobStorageService;

  @Value("${azure.storage.container-name}")
  private String containerName;

  public AddOnServiceImplementation(
      AddOnRepository addOnRepository, MediaService mediaService, BlobStorageService blobStorageService) {
    this.addOnRepository = addOnRepository;
    this.mediaService = mediaService;
    this.blobStorageService = blobStorageService;
  }

  private void generatePresignedUrl(AddOn addOn) {
    if (addOn.getMedia() != null) {
      String url = blobStorageService.generatePresignedUrl(containerName, addOn.getMedia().getBlobName());
      addOn.getMedia().setMediaUrl(url);
    }
  }

  /**
   * Construct a file name with extension
   *
   * @param baseName  - The base name to be used
   * @param mediaFile - The file content.
   * @return - The constructed string.
   */
  private String generateFileName(String baseName, MultipartFile mediaFile) {

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
   * @return All AddOns present in the database.
   */
  @Override
  public Page<AddOn> getAllAddOns(Map<String, String> params) {

    logger.info("Fetching addons...");
    double minItemPrice = 0d;
    Page<AddOn> addOns;

    try {
      if (params.isEmpty()) {
        Pageable pageable = Pageable.unpaged();
        addOns = addOnRepository.findAll(pageable);
      } else {

        CustomPage customPage = createCustomPageFromParams(params);

        if (params.containsKey("minItemPrice")) {
          minItemPrice = Double.parseDouble(params.get("minItemPrice"));
        }
        Pageable pageable =
            PageRequest.of(
                customPage.getPageNumber(),
                customPage.getPageSize(),
                customPage.getSortDirection(),
                customPage.getSortBy());

        addOns = addOnRepository.findAllAndFilter(minItemPrice, pageable);
        addOns.forEach(this::generatePresignedUrl);
      }
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (IllegalArgumentException iae) {
      logger.error(ILLEGAL_ARGUMENT_EXCEPTION_LOG_MESSAGE, iae);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BAD_REQUEST_RESPONSE_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "AddOns"));
    return addOns;
  }

  /**
   * Retrieve and AddOn from the database given the id.
   *
   * @param id The id of the targeted AddOn
   * @return An AddOn or null.
   */
  @Override
  public AddOn getAddOnById(Long id) {

    try {
      Optional<AddOn> optionalAddOn = addOnRepository.findById(id);
      if (optionalAddOn.isPresent()) {
        logger.info(String.format(GET_SUCCESS_MESSAGE, "AddOn", id));
        generatePresignedUrl(optionalAddOn.get());
        return optionalAddOn.get();
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(GET_NOT_FOUND_MESSAGE, "addOn", id));
    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "addOn", id));
  }

  /**
   * @param name Fetch AddOn from the database by its name.
   * @return The targeted AddOn
   */
  @Override
  public AddOn getAddOnByName(String name) {
    Optional<AddOn> addOn;
    try {
      addOn = addOnRepository.findByName(name);
      if (addOn.isPresent()) {
        logger.info(String.format(GET_BY_FIELD_SUCCESS_MESSAGE, "AddOn", "name", name));
        generatePresignedUrl(addOn.get());
        return addOn.get();
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.error(String.format(GET_BY_FIELD_NOT_FOUND_MESSAGE, "AddOn", "name", name));
    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND, String.format(GET_BY_FIELD_NOT_FOUND_MESSAGE, "AddOn", "name", name));

  }

  /**
   * @param newAddOn  The AddOn to persist in the database
   * @param mediaFile The media file associated with the addOn.
   * @return The newly created AddOn
   */
  @Override
  @Transactional
  public AddOn createAddOn(AddOn newAddOn, MultipartFile mediaFile) {
    try {

      if (newAddOn.getId() != null && addOnRepository.existsById(newAddOn.getId())) {
        logger.error(String.format(CREATE_CONFLICT_MESSAGE2, "addOn", newAddOn.getId()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(CREATE_CONFLICT_MESSAGE2, "addOn", newAddOn.getId()));
      }

      if (addOnRepository.existsByName(newAddOn.getName())) {
        logger.error(String.format(FIELD_CONFLICT_MESSAGE2, "addOn", "name", newAddOn.getName()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(FIELD_CONFLICT_MESSAGE2, "addOn", "name", newAddOn.getName()));
      }

      AddOnMedia addOnMedia;
      if (mediaFile != null) {
        String mediaFileName = generateFileName(newAddOn.getName(), mediaFile);
        addOnMedia = mediaService.saveAddOnMedia(new CustomMultipartFile(mediaFile, mediaFileName));
        newAddOn.setMedia(addOnMedia);
      }

      addOnRepository.save(newAddOn);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "AddOn"));
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (IOException ioe) {
      logger.error(IllegalAccessError_IOException_LOG_MESSAGE, ioe);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newAddOn;
  }

  /**
   * @param newAddOn  Updated AddOn information.
   * @param mediaFile The media file associated with the addOn.
   * @return The newly updated AddOn.
   */
  @Override
  @Transactional
  public AddOn updateAddOn(Long id, AddOn newAddOn, MultipartFile mediaFile) {

    if (!Objects.equals(id, newAddOn.getId())) {
      logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "addOn"));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "addOn"));
    }

    try {
      if (addOnRepository.existsById(id)) {
        AddOn existingAddOn = getAddOnById(id);
        existingAddOn.setName(newAddOn.getName());
        existingAddOn.setCategory(newAddOn.getCategory());
        existingAddOn.setDescription(newAddOn.getDescription());
        existingAddOn.setPrice(newAddOn.getPrice());
        existingAddOn.setActive(newAddOn.isActive());

        AddOnMedia addOnMedia;
        if (mediaFile != null) {
          try {
            if (existingAddOn.getMedia() == null) {
              existingAddOn.setMedia(new AddOnMedia());
              existingAddOn.getMedia().setAddOn(existingAddOn);
            }
            String mediaFileName = generateFileName(newAddOn.getName(), mediaFile);
            addOnMedia = mediaService.updateAddOnMedia(existingAddOn.getMedia(), new CustomMultipartFile(mediaFile, mediaFileName));
          } catch (IOException ioe) {
            logger.error(IllegalAccessError_IOException_LOG_MESSAGE, ioe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
          }
          existingAddOn.setMedia(addOnMedia);
        } else {
          if (existingAddOn.getMedia() != null) {
            String mediaUrl = existingAddOn.getMedia().getMediaUrl().split("\\?")[0];
            existingAddOn.getMedia().setMediaUrl(mediaUrl);
          }
        }


        addOnRepository.save(existingAddOn);
        logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "AddOn", id));
        return existingAddOn;
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "addOn"));
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(UPDATE_NOT_FOUND_MESSAGE, "addOn"));
  }

  /**
   * Delete a specific AddOn from the database.
   *
   * @param id The id of the targeted AddOn
   */
  @Override
  @Transactional
  public void deleteAddOn(Long id) {
    Optional<AddOn> addOn;
    try {
      addOn = addOnRepository.findById(id);
      if (addOn.isPresent()) {
        mediaService.deleteAddOnMediaFile(addOn.get().getMedia());
        addOnRepository.deleteById(id);
        logger.info(String.format(DELETE_SUCCESS_MESSAGE, "AddOn", id));
      } else {
        logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "addOn"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(DELETE_NOT_FOUND_MESSAGE, "addOn"));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

  }

  /**
   * Delete multiple instances of addOns.
   *
   * @param ids A list of addOn ids.
   */
  @Override
  public void deleteManyAddOns(List<Long> ids) {
    List<Long> existentAddOns = new ArrayList<>();
    StringBuilder nonexistentAddOns = new StringBuilder();

    try {
      ids.parallelStream()
          .forEach(
              id -> {
                if (addOnRepository.existsById(id)) {
                  existentAddOns.add(id);
                } else {
                  nonexistentAddOns.append(id).append(", ");
                }
              });

      if (!existentAddOns.isEmpty()) {
        existentAddOns.parallelStream().forEach(this::deleteAddOn);
      }

    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    if (!nonexistentAddOns.isEmpty()) {
      String errorMessageIds =
          Arrays.stream(nonexistentAddOns.toString().split(", "))
              .sorted()
              .collect(Collectors.joining(", "));
      logger.error(String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "addOns", errorMessageIds));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "addOns", errorMessageIds));
    }
  }
}