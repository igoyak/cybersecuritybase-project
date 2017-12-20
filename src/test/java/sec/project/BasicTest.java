package sec.project;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sec.project.repository.SignupRepository;

import javax.servlet.http.Cookie;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BasicTest {

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
        MvcResult res = mockMvc.perform(get("/404" + maliciousScript)).andExpect(status().is4xxClientError()).andReturn();
        String responseString = res.getResponse().getContentAsString();
        assertTrue("A malicious script in the URL should be returned in the HTML response", responseString.contains(maliciousScript));
    }


    @Test
    public void MissingAccessControlVulnerability() throws Throwable {
        assertTrue(signupRepository.findAll().size() != 0);
        System.out.println(signupRepository.findAll().size());
        MvcResult res = mockMvc.perform(post("/deleteall")).andExpect(status().is3xxRedirection()).andReturn();
        String responseString = res.getResponse().getContentAsString();
        System.out.println(responseString);
        System.out.println(signupRepository.findAll().size());
        assertTrue(signupRepository.findAll().size() == 0);
    }

    @Test
    public void CSRFVulnerability() throws Throwable {
        Long idToDelete = 1L;
        assertNotNull(signupRepository.findOne(idToDelete));
        MvcResult res = mockMvc.perform(get("/delete").cookie(new Cookie("accountid", String.valueOf(idToDelete)))).andExpect(status().is3xxRedirection()).andReturn();
        assertNull(signupRepository.findOne(idToDelete));
    }


}
