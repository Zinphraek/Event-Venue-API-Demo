package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.util.List;

import static com.zinphraek.leprestigehall.domain.constants.Constants.GENDERS;

public class UserFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();

  public User generateRandomUser(String userId) {
    User user = new User();
    List<String> genderList = GENDERS;
    user.setId(utilities.generateRandomLong());
    user.setFirstName(utilities.generateRandomString());
    user.setLastName(utilities.generateRandomString());
    user.setPhone(utilities.generateRandomPhoneNumber());
    user.setEmail(utilities.generateRandomEmail());
    user.setDateOfBirth(
        utilities.formatLocalDateTime(utilities.generateRandomPastLocalDateTime(30L)));
    user.setGender(
        genderList.get(utilities.generateRandomIntBetween0AndNExclusive(genderList.size())));
    user.setUserId(userId);
    return user;
  }

  public UserSummaryDTO generateRandomUserSummaryDTO(boolean shouldHaveUserId) {
    return new UserSummaryDTO(
        shouldHaveUserId ? utilities.generateRandomStringWithDefinedLength(16) : null,
        utilities.generateRandomString(), utilities.generateRandomString(), null);
  }
}
