package com.zinphraek.leprestigehall.domain.addon;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AddOnControllerTest {

  /*
  @LocalServerPort
  private int port;
  @Autowired
  private MockMvc mockMvc;
  AddonFactory addonFactory = new AddonFactory();
  @Autowired
  private AddOnController addOnController;
  @Autowired
  private AddOnRepository addOnRepository;
  FactoriesUtilities utilities = new FactoriesUtilities();
  @Autowired
  private AddOnServiceImplementation addOnService;

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
    addOnRepository.deleteAll();
  }

   Add a test for the getAllAddOns method.
  @Test
  public void getAllAddOnsTestSuccessfulCase() throws Exception {
    List<AddOn> addOns = addonFactory.generateListOfRandomAddons(1, 10, 5);
    /* addOnRepository.saveAll(addOns);
    mockMvc
        .perform(get(AddOnPath).param(new HashMap<>().toString()))
        .andExpect(status().isOk())
        .andExpect(content().json(addOns.toString()));
  }
  */
}
