package sec.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sec.project.repository.SignupRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleTest {

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private SignupRepository signupRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }

    @Test
    public void vuln1XSS() throws Throwable {
        String maliciousScript = "%3Cscript%3Ealert(document.cookie);%3C/script%3E";
        MvcResult res = mockMvc.perform(get("/404" + maliciousScript)).andExpect(status().is2xxSuccessful()).andReturn();
        String responseString = res.getResponse().getContentAsString();
        assertTrue("A malicious script in the URL should be returned in the HTML response", responseString.contains(maliciousScript));
    }

    @Test
    public void signupAddsDataToDatabase() throws Throwable {
        mockMvc.perform(post("/form").param("name", "Testname").param("address", "Testaddress")).andReturn();
        assertEquals(1L, signupRepository.findAll().stream().filter(s -> s.getName().equals("Testname") && s.getAddress().equals("Testaddress")).count());
    }

}
