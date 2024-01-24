package com.zinphraek.leprestigehall.domain.addon;

import com.zinphraek.leprestigehall.domain.blobstorage.BlobStorageService;
import com.zinphraek.leprestigehall.domain.data.factories.AddonFactory;
import com.zinphraek.leprestigehall.domain.data.factories.MediaFactory;
import com.zinphraek.leprestigehall.domain.media.AddOnMedia;
import com.zinphraek.leprestigehall.domain.media.MediaService;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AddOnServiceImplementation}.
 */
@ExtendWith(MockitoExtension.class)
public class AddOnServiceImplementationTest {

  FactoriesUtilities utilities = new FactoriesUtilities();
  AddonFactory addonFactory = new AddonFactory();
  MediaFactory mediaFactory = new MediaFactory();
  @Mock
  private AddOnRepository addOnRepository;
  @Mock
  private MediaService mediaService;
  @Mock
  private BlobStorageService blobStorageService;

  @InjectMocks
  private AddOnServiceImplementation addOnService;

  @BeforeEach
  void setUp() {
    addOnService =
        new AddOnServiceImplementation(
            this.addOnRepository, this.mediaService, this.blobStorageService);
  }

  @Test
  void createAddOnTestSuccessfulCase() throws IOException {
    AddOn addOn = addonFactory.generateRandomAddon(1L);
    AddOnMedia addOnMedia = mediaFactory.generateARandomAddOnMedia(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.mediaService.saveAddOnMedia(any())).thenReturn(addOnMedia);
    when(this.addOnRepository.save(any())).thenReturn(addOn);
    when(this.addOnRepository.existsById(any())).thenReturn(false);
    when(this.addOnRepository.existsByName(any())).thenReturn(false);
    AddOn createdAddOn = this.addOnService.createAddOn(addOn, multipartFile);
    assert createdAddOn != null;
    assert createdAddOn.getName().equals(addOn.getName());
    assert createdAddOn.getDescription().equals(addOn.getDescription());
    assert createdAddOn.getPrice() == (addOn.getPrice());
    assert createdAddOn.getName().equals(addOn.getName());
    assert createdAddOn.getDescription().equals(addOn.getDescription());
    assert createdAddOn.getPrice() == (addOn.getPrice());
    assert createdAddOn.getMedia().getMediaUrl().equals(addOnMedia.getMediaUrl());

    verify(addOnRepository, times(1)).save(any());
  }

  @Test
  void createAddOnTestWithExistingName() {
    AddOn addOn = addonFactory.generateAddOnWithSpecificName("Test");
    String expectedReason =
        "An addOn with name: " + addOn.getName() + " already exists in the database.";
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.addOnRepository.existsByName(any())).thenReturn(true);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.createAddOn(addOn, multipartFile));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(expectedReason, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void createAddOnTestWithExistingId() {
    AddOn addOn = addonFactory.generateRandomAddon(1L);
    String expectedReason =
        "An addOn with id: " + addOn.getId() + " already exists in the database.";
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.addOnRepository.existsById(any())).thenReturn(true);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.createAddOn(addOn, multipartFile));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(expectedReason, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void createAddOnTestDataAccessExceptionCase() throws IOException {
    AddOn addOn = addonFactory.generateRandomAddon(1L);
    AddOnMedia addOnMedia = mediaFactory.generateARandomAddOnMedia(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.mediaService.saveAddOnMedia(any())).thenReturn(addOnMedia);
    when(this.addOnRepository.save(any())).thenThrow(new DataAccessException("Database error") {
    });
    when(this.addOnRepository.existsById(any())).thenReturn(false);
    when(this.addOnRepository.existsByName(any())).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.createAddOn(addOn, multipartFile));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).save(any());
  }

  @Test
  void createAddOnTestIOExceptionCase() throws IOException {
    AddOn addOn = addonFactory.generateRandomAddon(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.mediaService.saveAddOnMedia(any()))
        .thenThrow(new IOException("Input/Output Exception") {
        });
    when(this.addOnRepository.existsById(any())).thenReturn(false);
    when(this.addOnRepository.existsByName(any())).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.createAddOn(addOn, multipartFile));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void createAddOnTestGenericExceptionCase() throws IOException {
    AddOn addOn = addonFactory.generateRandomAddon(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);
    when(this.mediaService.saveAddOnMedia(any()))
        .thenThrow(new RuntimeException("Generic Exception") {
        });

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.createAddOn(addOn, multipartFile));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void getAddOnByNameTestSuccessfulCase() {
    String name = "Test";
    AddOn addOn = addonFactory.generateAddOnWithSpecificName(name);
    when(this.addOnRepository.findByName(any())).thenReturn(Optional.ofNullable(addOn));
    AddOn retreivedAddOn = this.addOnService.getAddOnByName(name);
    assertNotNull(retreivedAddOn);
    assertNotNull(addOn);
    assertEquals(addOn.getId(), retreivedAddOn.getId());
    assertEquals(addOn.getName(), retreivedAddOn.getName());
    assertEquals(addOn.getPrice(), retreivedAddOn.getPrice());
    assertEquals(addOn.getDescription(), retreivedAddOn.getDescription());

    verify(addOnRepository, times(1)).findByName(any());
  }

  @Test
  void getAddOnByNameTestNoFoundCase() {
    String name = "Test";
    String expectedReason = "No AddOn with name: " + name + " found in the database.";
    when(this.addOnRepository.findByName(any())).thenReturn(Optional.empty());
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnByName(name));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(expectedReason, exception.getReason());

    verify(addOnRepository, times(1)).findByName(any());
  }

  @Test
  void getAddOnByNameTestDataAccessExceptionCase() {
    String name = "Test";
    when(this.addOnRepository.findByName(name))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnByName(name));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findByName(any());
  }

  @Test
  void getAddOnByNameTestGenericExceptionCase() {
    String name = "Test";
    when(this.addOnRepository.findByName(name))
        .thenThrow(new RuntimeException("Generic Exception") {
        });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnByName(name));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findByName(any());
  }

  @Test
  void getAddOnByIdTestSuccessfulCase() {
    long id = 1L;
    AddOn addOn = addonFactory.generateRandomAddon(id);
    when(this.addOnRepository.findById(any())).thenReturn(Optional.ofNullable(addOn));
    AddOn retreivedAddOn = this.addOnService.getAddOnById(id);
    assertNotNull(retreivedAddOn);
    assertNotNull(addOn);
    assertEquals(addOn.getId(), retreivedAddOn.getId());
    assertEquals(addOn.getName(), retreivedAddOn.getName());
    assertEquals(addOn.getPrice(), retreivedAddOn.getPrice());
    assertEquals(addOn.getDescription(), retreivedAddOn.getDescription());

    verify(addOnRepository, times(1)).findById(any());
  }

  @Test
  void getAddOnByIdTestNotFoundCase() {
    long id = 1L;
    String expectedReasonMessage = "No addOn with id: " + id + " found in the database.";
    when(this.addOnRepository.findById(id)).thenReturn(Optional.empty());
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnById(id));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(expectedReasonMessage, exception.getReason());

    verify(addOnRepository, times(1)).findById(any());
  }

  @Test
  void getAddOnByIdTestDataAccessExceptionCase() {
    long id = 1L;
    when(this.addOnRepository.findById(id)).thenThrow(new DataAccessException("Database error") {
    });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnById(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findById(any());
  }

  @Test
  void getAddOnByIdTestGenericExceptionCase() {
    long id = 1L;
    when(this.addOnRepository.findById(id)).thenThrow(new RuntimeException("Generic Exception") {
    });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAddOnById(id));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findById(any());
  }

  @Test
  void deleteAddOnTestSuccessfulCase() {
    long id = 1L;
    AddOn addOn = addonFactory.generateRandomAddon(id);
    when(this.addOnRepository.findById(id)).thenReturn(Optional.ofNullable(addOn));
    this.addOnService.deleteAddOn(id);

    verify(addOnRepository, times(1)).deleteById(id);
  }

  @Test
  void deleteAddOnTestNotFoundCase() {
    long id = 1L;
    String expectedReasonMessage = "Could not delete non existent addOn.";
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteAddOn(id));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(expectedReasonMessage, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(id);
  }

  @Test
  void deleteAddOnTestDataAccessExceptionCase1() {
    long id = 1L;
    when(this.addOnRepository.findById(id)).thenThrow(new DataAccessException("Database error") {
    });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteAddOn(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(id);
  }

  @Test
  void deleteAddOnTestDataAccessExceptionCase2() {
    long id = 1L;
    AddOn addOn = addonFactory.generateRandomAddon(id);
    when(this.addOnRepository.findById(id)).thenReturn(Optional.ofNullable(addOn));
    doThrow(new DataAccessException("Database error") {
    }).when(addOnRepository).deleteById(id);
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteAddOn(id));
    verify(addOnRepository, times(1)).deleteById(id); // Verifies the method was called exactly once
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).deleteById(id);
  }

  @Test
  void deleteAddOnTestGenericExceptionCase() {
    long id = 1L;
    when(this.addOnRepository.findById(id)).thenThrow(new RuntimeException("Generic Exception") {
    });
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteAddOn(id));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(id);
  }

  @Test
  void deleteManyAddOnsTestSuccessfulCase() {
    List<Long> ids = utilities.generateListOfRandomDistinctLongs(3);

    for (Long id : ids) {
      AddOn addOn = addonFactory.generateRandomAddon(id);

      when(addOnRepository.existsById(id)).thenReturn(true);
      when(addOnRepository.findById(id)).thenReturn(Optional.ofNullable(addOn));
    }

    addOnService.deleteManyAddOns(ids);

    verify(addOnRepository, times(3)).deleteById(any());
  }

  @Test
  void deleteManyAddOnsTestPartiallySuccessfulAndNotFoundCase() {
    List<Long> ids = utilities.generateListOfNDistinctRandomNumbersInRange(1, 10, 5);
    StringBuilder expectedErrorLogMessage = new StringBuilder();
    expectedErrorLogMessage.append("Could not delete the following addOns with ids: ");

    for (Long id : ids.subList(0, 3)) {
      AddOn addOn = addonFactory.generateRandomAddon(id);

      when(addOnRepository.existsById(id)).thenReturn(true);
      when(addOnRepository.findById(id)).thenReturn(Optional.ofNullable(addOn));
    }

    for (Long id : ids.subList(3, 5).stream().sorted().toList()) {
      expectedErrorLogMessage.append(id).append(", ");
      when(addOnRepository.existsById(id)).thenReturn(false);
    }
    expectedErrorLogMessage.append("as they do not exist in the database");

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteManyAddOns(ids));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(expectedErrorLogMessage.toString(), exception.getReason());

    verify(addOnRepository, times(3)).deleteById(any());
  }

  @Test
  void deleteManyAddOnsTestNotFoundCase() {
    List<Long> ids = utilities.generateListOfNDistinctRandomNumbersInRange(1, 10, 5).stream().sorted().toList();
    StringBuilder expectedErrorLogMessage = new StringBuilder();
    expectedErrorLogMessage.append("Could not delete the following addOns with ids: ");

    for (Long id : ids) {
      expectedErrorLogMessage.append(id).append(", ");
      when(addOnRepository.existsById(id)).thenReturn(false);
    }
    expectedErrorLogMessage.append("as they do not exist in the database");

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteManyAddOns(ids));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(expectedErrorLogMessage.toString(), exception.getReason());

    verify(addOnRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteManyAddOnsTestDataAccessExceptionCase1() {
    List<Long> ids = utilities.generateListOfNDistinctRandomNumbersInRange(1, 10, 5).stream().sorted().toList();
    when(addOnRepository.existsById(any())).thenThrow(new DataAccessException("Database error") {
    });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteManyAddOns(ids));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteManyAddOnsTestDataAccessExceptionCase2() {
    List<Long> ids = utilities.generateListOfNDistinctRandomNumbersInRange(1, 10, 5).stream().sorted().toList();

    when(addOnRepository.existsById(any())).thenReturn(true);
    when(addOnRepository.findById(any())).thenThrow(new DataAccessException("Database error") {
    });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteManyAddOns(ids));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteManyAddOnsTestGenericExceptionCase() {
    List<Long> ids = utilities.generateListOfNDistinctRandomNumbersInRange(1, 10, 5).stream().sorted().toList();
    when(addOnRepository.existsById(any())).thenThrow(new RuntimeException("Generic Exception") {
    });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.deleteManyAddOns(ids));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).deleteById(any());
  }

  @Test
  void updateAddOnTestSuccessfulCase() throws IOException {
    long id = 1L;
    AddOn existingAddOn = addonFactory.generateRandomAddon(id);
    AddOn updatedAddOn = addonFactory.generateRandomAddon(id);
    AddOnMedia addOnMedia = mediaFactory.generateARandomAddOnMedia(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(this.addOnRepository.existsById(id)).thenReturn(true);
    when(this.addOnRepository.findById(id)).thenReturn(Optional.ofNullable(existingAddOn));
    when(this.addOnRepository.save(any())).thenReturn(updatedAddOn);
    when(this.mediaService.updateAddOnMedia(any(), any())).thenReturn(addOnMedia);

    AddOn returnedAddOn = this.addOnService.updateAddOn(id, updatedAddOn, multipartFile);
    assertNotNull(returnedAddOn);
    assertEquals(updatedAddOn.getId(), returnedAddOn.getId());
    assertEquals(updatedAddOn.getName(), returnedAddOn.getName());
    assertEquals(updatedAddOn.getPrice(), returnedAddOn.getPrice());
    assertEquals(updatedAddOn.getDescription(), returnedAddOn.getDescription());

    verify(addOnRepository, times(1)).save(any());
  }

  @Test
  void updateAddOnTestNonMatchingIdsCase() {
    long id = 1L;
    AddOn updatedAddOn = addonFactory.generateRandomAddon(id + 1);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.updateAddOn(id, updatedAddOn, null));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "addOn"), exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void updateAddOnTestNonExistentAddOnCase() {
    long id = 1L;
    AddOn updatedAddOn = addonFactory.generateRandomAddon(id);
    String expectedReasonMessage = "Could not update non existent addOn.";
    when(this.addOnRepository.existsById(id)).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.updateAddOn(id, updatedAddOn, null));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(expectedReasonMessage, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void updateAddOnTestIOExceptionCase() throws IOException {
    long id = 1L;
    AddOn existingAddOn = addonFactory.generateRandomAddon(id);
    AddOn updatedAddOn = addonFactory.generateRandomAddon(id);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(this.addOnRepository.existsById(id)).thenReturn(true);
    when(this.addOnRepository.findById(id)).thenReturn(Optional.ofNullable(existingAddOn));
    when(this.mediaService.updateAddOnMedia(any(), any()))
        .thenThrow(new IOException("Input/Output Exception") {
        });

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.updateAddOn(id, updatedAddOn, multipartFile));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).save(any());
  }

  @Test
  void updateAddOnTestDataAccessExceptionCase() throws IOException {
    long id = 1L;
    AddOn existingAddOn = addonFactory.generateRandomAddon(id);
    AddOn updatedAddOn = addonFactory.generateRandomAddon(id);
    AddOnMedia addOnMedia = mediaFactory.generateARandomAddOnMedia(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(this.addOnRepository.existsById(id)).thenReturn(true);
    when(this.addOnRepository.findById(id)).thenReturn(Optional.ofNullable(existingAddOn));
    when(this.mediaService.updateAddOnMedia(any(), any())).thenReturn(addOnMedia);
    when(this.addOnRepository.save(any())).thenThrow(new DataAccessException("Database error") {
    });

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.addOnService.updateAddOn(id, updatedAddOn, multipartFile));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).save(any());
  }

  @Test
  void getAllAddOnsTestSuccessfulEmptyPageCase() {
    Map<String, String> params = new HashMap<>();
    List<AddOn> expectedAddOns = new ArrayList<>();
    Page<AddOn> mockPage = new PageImpl<>(expectedAddOns);

    when(addOnRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

    Page<AddOn> returnedPage = this.addOnService.getAllAddOns(params);
    assertNotNull(returnedPage);
    assertEquals(expectedAddOns, returnedPage.getContent());
    assertEquals(0, returnedPage.getTotalElements());
    assertEquals(1, returnedPage.getTotalPages());
    assertEquals(0, returnedPage.getNumber());

    verify(addOnRepository, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestSuccessfulNonEmptyPageCase() {
    Map<String, String> params = utilities.getCustomPageTestParams("name", "10");
    List<AddOn> expectedAddOns = new ArrayList<>(addonFactory.generateListOfRandomAddons(1, 12, 10));
    AddOn addOn = addonFactory.generateRandomAddon(13L);
    AddOnMedia addOnMedia = mediaFactory.generateARandomAddOnMedia(1L);
    addOn.setMedia(addOnMedia);
    addOnMedia.setAddOn(addOn);
    expectedAddOns.add(addOn);
    Page<AddOn> mockPage = new PageImpl<>(expectedAddOns);
    params.put("minItemPrice", "0");

    //when(blobServiceClient.generatePresignedUrl(any())).thenReturn(utilities.generateRandomURL());
    when(addOnRepository.findAllAndFilter(any(Double.class), any(Pageable.class)))
        .thenReturn(mockPage);

    Page<AddOn> returnedPage = this.addOnService.getAllAddOns(params);
    assertNotNull(returnedPage);
    assertEquals(expectedAddOns, returnedPage.getContent());
    assertEquals(expectedAddOns.size(), returnedPage.getTotalElements());
    assertEquals(1, returnedPage.getTotalPages());
    assertEquals(0, returnedPage.getNumber());

    verify(addOnRepository, times(1)).findAllAndFilter(any(Double.class), any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestEmptyPageDataAccessExceptionCase() {
    Map<String, String> params = new HashMap<>();
    when(addOnRepository.findAll(any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAllAddOns(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestNonEmptyPageDataAccessExceptionCase() {
    Map<String, String> params = utilities.getCustomPageTestParams("name", "10");
    params.put("minItemPrice", "0");
    when(addOnRepository.findAllAndFilter(any(Double.class), any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAllAddOns(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findAllAndFilter(any(Double.class), any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestEmptyPageGenericExceptionCase() {
    Map<String, String> params = new HashMap<>();
    when(addOnRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Generic Exception") {
    });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAllAddOns(params));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findAll(any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestNonEmptyPageGenericExceptionCase() {
    Map<String, String> params = utilities.getCustomPageTestParams("name", "10");
    params.put("minItemPrice", "0");
    when(addOnRepository.findAllAndFilter(any(Double.class), any(Pageable.class)))
        .thenThrow(new RuntimeException("Generic Exception") {
        });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAllAddOns(params));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(1)).findAllAndFilter(any(Double.class), any(Pageable.class));
  }

  @Test
  void getAllAddOnsTestNonEmptyPageIllegalArgumentExceptionCase() {
    Map<String, String> params = utilities.getCustomPageTestParams("name", "10");
    params.put("minItemPrice", "0");
    when(addOnRepository.findAllAndFilter(any(Double.class), any(Pageable.class)))
        .thenThrow(new IllegalArgumentException("Generic Exception") {
        });

    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> this.addOnService.getAllAddOns(params));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(BAD_REQUEST_RESPONSE_ERROR_MESSAGE, exception.getReason());

    verify(addOnRepository, times(0)).findAll(any(Pageable.class));
    verify(addOnRepository, times(1)).findAllAndFilter(any(Double.class), any(Pageable.class));
  }
}