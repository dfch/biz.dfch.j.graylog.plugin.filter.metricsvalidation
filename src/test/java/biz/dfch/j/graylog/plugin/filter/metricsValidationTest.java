package biz.dfch.j.graylog.plugin.filter;

/**
 * Created by root on 3/23/15.
 */

import biz.dfch.j.graylog.plugin.filter.metricsValidation;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.graylog2.plugin.Message;
import org.joda.time.DateTime;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class metricsValidationTest 
{
    @BeforeClass
    public static void BeforeClass()
    {
        System.out.println("BeforeClass");
    }
    
    @Before
    public void Before()
    {
        System.out.println("Before");
    }
    
    @Test
    public void doNothingReturnsTrue()
    {
        String fn = Thread.currentThread().getStackTrace()[1].getMethodName();
        System.out.println(String.format("%s: CALL.", fn));

        assertEquals(true, true);

        System.out.println(String.format("%s: RET.", fn));
    }

//    @Test
//    public void doFilterMessageReturnsTrue()
//            throws URISyntaxException, IOException
//    {
//        metricsValidation _mv = new metricsValidation();
//        Message message = new Message("myMessage", "mySource", new DateTime());
//        _mv.filter(message);
//
//        String fn = Thread.currentThread().getStackTrace()[1].getMethodName();
//        System.out.println(String.format("%s: CALL.", fn));
//
//        assertEquals(true, true);
//
//        System.out.println(String.format("%s: RET.", fn));
//    }
//
    @Test
    public void doInitialisePluginReturnsTrue()
            throws IOException, URISyntaxException
    {
        String fn = Thread.currentThread().getStackTrace()[1].getMethodName();
        System.out.println(String.format("%s: CALL.", fn));
        
        metricsValidation mv = new metricsValidation();

        assertEquals(true, true);

        System.out.println(String.format("%s: RET.", fn));
    }

    @After
    public void After() throws Throwable
    {
        System.out.println("After");
    }
}
