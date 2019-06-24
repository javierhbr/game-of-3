package com.takeaway.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.web.SpringBootMockServletContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TakeawayChallengeApplication.class)
public class TestApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	public void whenContextLoads_thenAllBeanCreated() {
		ServletContext servletContext = webApplicationContext.getServletContext();

		assertThat(servletContext, is(notNullValue()));
		assertThat(servletContext, instanceOf(SpringBootMockServletContext.class));
		assertThat(webApplicationContext.getBean("gameController"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("playController"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("gameService"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("playService"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("playerBTurnListener"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("senderPlayA"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("senderPlayB"), is(notNullValue()));
		assertThat(webApplicationContext.getBean("playMessageConverter"), is(notNullValue()));

	}

}
