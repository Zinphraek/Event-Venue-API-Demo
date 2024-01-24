package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.addon.AddOn;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.util.List;

public class AddonFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();

  /**
   * Generates a random add-on.
   *
   * @param id the id of the add-on.
   * @return a random add-on.
   */
  public AddOn generateRandomAddon(long id) {
    AddOn addOn = new AddOn();
    addOn.setId(id != 0 ? id : null);
    addOn.setName(utilities.generateRandomString());
    addOn.setDescription(utilities.generateRandomString());
    addOn.setPrice(utilities.generateRandomDoubleWithin(1D, 100D));
    addOn.setActive(true);
    addOn.setCategory(utilities.generateRandomString());
    return addOn;
  }

  /**
   * Generates a random add-on with a specific name.
   *
   * @param name the name of the add-on.
   * @return a random add-on.
   */
  public AddOn generateAddOnWithSpecificName(String name) {
    AddOn addOn = new AddOn();
    addOn.setName(name);
    addOn.setDescription(utilities.generateRandomString());
    addOn.setPrice(utilities.generateRandomDoubleWithin(1D, 100D));
    addOn.setActive(true);
    addOn.setCategory(utilities.generateRandomString());
    return addOn;
  }

  /**
   * Generates a random requested add-on.
   *
   * @param id       the id of the requested add-on.
   * @param quantity the quantity of the requested add-on.
   * @return a random requested add-on.
   */
  public RequestedAddOn generateRandomRequestedAddOn(long id, double quantity) {
    RequestedAddOn requestedAddOn = new RequestedAddOn();
    requestedAddOn.setId(id != 0 ? id : null);
    requestedAddOn.setAddOn(generateRandomAddon(utilities.getRandomPositiveLong(1L, 100L)));
    requestedAddOn.setQuantity(quantity);
    return requestedAddOn;
  }

  /**
   * Generate a list of random requested add-ons.
   *
   * @param minIdValue the minimum value an id can be.
   * @param maxIdValue the maximum value an id can be.
   * @param listSize   the size of the list.
   * @return a list of random requested add-ons.
   */
  public List<RequestedAddOn> generateListOfRandomRequestedAddOns(int minIdValue, int maxIdValue, int listSize) {
    return utilities
        .generateListOfNDistinctRandomNumbersInRange(minIdValue, maxIdValue, listSize)
        .stream()
        .map(id -> generateRandomRequestedAddOn(id, utilities.generateRandomDoubleWithin(1D, 100D)))
        .toList();
  }

  /**
   * Generates a specified size list of distinct random add-ons with ids in the specified range.
   *
   * @param minIdValue the minimum value an id can be.
   * @param maxIdValue the maximum value an id can be.
   * @param listSize   the size of the list.
   * @return a list of random add-ons.
   */
  public List<AddOn> generateListOfRandomAddons(int minIdValue, int maxIdValue, int listSize) {
    return utilities
        .generateListOfNDistinctRandomNumbersInRange(minIdValue, maxIdValue, listSize)
        .stream()
        .map(this::generateRandomAddon)
        .toList();
  }
}
