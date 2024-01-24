package com.zinphraek.leprestigehall.domain.addon;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface AddOnService {

  Page<AddOn> getAllAddOns(Map<String, String> params);

  AddOn getAddOnById(Long id);

  AddOn getAddOnByName(String name);

  @Transactional
  AddOn createAddOn(AddOn newAddOn, MultipartFile mediaFile);

  @Transactional
  AddOn updateAddOn(Long id, AddOn newAddOn, MultipartFile mediaFile);

  @Transactional
  void deleteAddOn(Long id);

  void deleteManyAddOns(List<Long> ids);
}
