package com.sap.hcp.cf.logback.converter;

import static com.sap.hcp.cf.logging.common.customfields.CustomField.customField;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sap.hcp.cf.logging.common.converter.DefaultCustomFieldsConverter;
import com.sap.hcp.cf.logging.common.customfields.CustomField;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;

@RunWith(MockitoJUnitRunner.class)
public class CustomFieldsConverterTest extends AbstractConverterTest {

	@SuppressWarnings("serial")
	private static Map<String, String> MDC_PROPERTIES = new HashMap<String, String>() {
		{
			put("mdc key", "mdc value");
		}
	};

	@Mock
	private ILoggingEvent event;

	@Mock
	private Context context;

	@Mock
	private DefaultCustomFieldsConverter defaultConverter;

	@Mock
	private CustomFieldsAdapter customFieldsAdapter;

	@Captor
	private ArgumentCaptor<Map<String, String>> mdcFields;

	@Captor
	private ArgumentCaptor<Object> arguments;

	@InjectMocks
	private CustomFieldsConverter converter;

	@Before
	public void initConverter() {
		converter.setContext(context);
		converter.setConverter(defaultConverter);
		converter.setCustomFieldsAdapter(customFieldsAdapter);
	}

	@Test
	public void emptyMdcAndArguments() throws Exception {
		converter.start();

		converter.convert(event);

		verifyConverterCall(emptyMap(), nullValue());
	}



	@Test
	public void standardArgument() throws Exception {
		converter.start();
		mockArgumentArray(event, "an argument");

		converter.convert(event);

		verifyConverterCall(emptyMap(), is("an argument"));
	}


	@Test
	public void singleCustomFieldArgument() throws Exception {
		converter.start();
		CustomField customField = customField("some key", "some value");
		mockArgumentArray(event, customField);

		converter.convert(event);

		verifyConverterCall(emptyMap(), sameInstance(customField));
	}

	@Test
	public void propagatesPrefix() throws Exception {
		converter.setOptionList(asList("prefix"));

		converter.start();

		verify(defaultConverter).setFieldName("prefix");
	}



	@Test
	public void singleMdcField() throws Exception {
		when(event.getMDCPropertyMap()).thenReturn(MDC_PROPERTIES);
		converter.start();

		converter.convert(event);

		verifyConverterCall(hasEntry("mdc key", "mdc value"), nullValue());
	}

	@Test
	public void mergesMdcFieldsAndArguments() throws Exception {
		when(event.getMDCPropertyMap()).thenReturn(MDC_PROPERTIES);
		converter.start();

		CustomField customField = customField("some key", "some value");
		mockArgumentArray(event, customField);

		converter.convert(event);

		verifyConverterCall(hasEntry("mdc key", "mdc value"), sameInstance(customField));
	}

	private static void mockArgumentArray(ILoggingEvent event, Object... arguments) {
		when(event.getArgumentArray()).thenReturn(arguments);
	}

	private static Matcher<Map<? extends String, ? extends String>> emptyMap() {
		return not(hasEntry(anything(), anything()));
	}

	private void verifyConverterCall(Matcher<Map<? extends String, ? extends String>> mdcFieldsMatcher,
			Matcher<Object> argumentsMatcher) {
		verify(defaultConverter).convert(any(), mdcFields.capture(), arguments.capture());
		assertThat(mdcFields.getValue(), mdcFieldsMatcher);
		assertThat(arguments.getValue(), argumentsMatcher);
	}
}
