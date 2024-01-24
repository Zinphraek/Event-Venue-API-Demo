package com.zinphraek.leprestigehall.domain.addon;

import static com.zinphraek.leprestigehall.domain.constants.Paths.AddOnPath;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/** End points to manage Addons */
@Controller
public class AddOnController {

  @Autowired private final AddOnServiceImplementation addOnService;

  public AddOnController(AddOnServiceImplementation addOnService) {
    this.addOnService = addOnService;
  }

  @GetMapping(AddOnPath)
  public ResponseEntity<Page<AddOn>> getAllAddOn(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(addOnService.getAllAddOns(params), HttpStatus.OK);
  }

  @GetMapping(AddOnPath + "/{id}")
  public ResponseEntity<AddOn> getAddOnById(@PathVariable Long id) {
    return new ResponseEntity<>(addOnService.getAddOnById(id), HttpStatus.OK);
  }

  @PostMapping(AddOnPath)
  public ResponseEntity<AddOn> createAddOn(
      @RequestPart("addOn") AddOn addOn,
      @RequestPart(name = "imageFile", required = false) MultipartFile mediaFile) {
    return new ResponseEntity<>(addOnService.createAddOn(addOn, mediaFile), HttpStatus.CREATED);
  }

  @PutMapping(AddOnPath + "/{id}")
  public ResponseEntity<AddOn> updateAddOn(
      @PathVariable Long id,
      @RequestPart("addOn") AddOn addOn,
      @RequestPart(name = "imageFile", required = false) MultipartFile mediaFile) {
    return new ResponseEntity<>(addOnService.updateAddOn(id, addOn, mediaFile), HttpStatus.OK);
  }

  @DeleteMapping(AddOnPath + "/{id}")
  public ResponseEntity<Void> deleteAddOn(@PathVariable Long id) {
    addOnService.deleteAddOn(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping(AddOnPath)
  public ResponseEntity<Void> deleteManyAddOns(@RequestPart("ids") List<Long> ids) {
    addOnService.deleteManyAddOns(ids);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
