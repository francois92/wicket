/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.validation;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.html.form.FormComponent;
import wicket.util.convert.ConversionException;
import wicket.util.convert.ConversionUtils;
import wicket.util.convert.ConverterRegistry;

/**
 * Validates input by trying it to convert to the given type using the
 * {@link wicket.util.convert.ConverterRegistry}instance of that can be found in the
 * application settings.
 * @author Eelco Hillenius
 */
public class TypeValidator extends AbstractValidator
{
	/** Log. */
	private static Log log = LogFactory.getLog(TypeValidator.class);

	/**
	 * The locale to use. If null and useLocaled == true, the session's locale will be
	 * used.
	 */
	private Locale locale = null;

	/** The type to use for checking. */
	private Class type;

	/** Whether to use either the set locale or the session's locale. */
	private boolean useLocalized = true;

	/**
	 * Construct. The current session's locale will be used for conversion.
	 * @param type the type to use for checking
	 */
	public TypeValidator(final Class type)
	{
		this(type, null);
	}

	/**
	 * Construct. If useLocalized is true, the current session's locale will be used for
	 * conversion. If useLocalized is false, no localization will be used for checking.
	 * @param type the type to use for checking
	 * @param useLocalized whether localization (using the current session's locale) should
	 *           be used
	 */
	public TypeValidator(final Class type, final boolean useLocalized)
	{
		this(type, null);
		this.useLocalized = useLocalized;
	}

	/**
	 * Construct. If not-null, the given locale will be used for conversion. Otherwise the
	 * session's locale will be used for conversion.
	 * @param type the type to use for checking
	 * @param locale the locale to use
	 */
	public TypeValidator(final Class type, final Locale locale)
	{
		this.type = type;
		this.locale = locale;
	}

	/**
	 * Validates input by trying it to convert to the given type using the
	 * {@link wicket.util.convert.ConverterRegistry}instance of that can be found in the
	 * application settings.
	 * @param component the component that wants to validate its input
	 * @return validation error message
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public final ValidationErrorMessage validate(FormComponent component)
	{
		// Get component value
		final String value = component.getRequestString();
		if (isNullOrEmpty(value))
			return ValidationErrorMessage.NO_MESSAGE;
		// Check value by attempting to convert it
		final ConversionUtils conversionUtils = getConversionUtils();
		final Locale localeForValidation = getLocaleForValidation();
		try
		{
			conversionUtils.convert(component.getRequestString(), type, localeForValidation);
			return ValidationErrorMessage.NO_MESSAGE;
		}
		catch (ConversionException e)
		{
			return getError(value, component, e);
		}
	}

	/**
	 * Gets the conversion utilities.
	 * @return the conversion utilities
	 */
	protected final ConversionUtils getConversionUtils()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null)
		{
			ConverterRegistry converterRegistry = requestCycle.getApplication().getConverterRegistry();
			return converterRegistry.getConversionUtils();
		}
		else
		// we must be in a test case; just give a dummy
		{
			log.error("no current request cycle found; using a dummy converter registry");
			return new ConversionUtils(new ConverterRegistry());
		}
	}

	/**
	 * Gets the error message.
	 * @param input The input
	 * @param component the component
	 * @param e the conversion exception
	 * @return the validation error message
	 */
	protected ValidationErrorMessage getError(String input, FormComponent component,
			ConversionException e)
	{
		Map ctx = getMessageContext(input, component, e);
		return errorMessage(getResourceKey(component), ctx, input, component);
	}

	/**
	 * Gets the message context.
	 * @param input The input
	 * @param component the component
	 * @param e the conversion exception
	 * @return a map with variables for interpolation
	 */
	protected Map getMessageContext(String input, FormComponent component, ConversionException e)
	{
		Map ctx = super.getMessageContextVariables(input, component);
		ctx.put("type", type);
		Locale loc = e.getLocale();
		if (loc != null)
			ctx.put("locale", loc);
		ctx.put("exception", e.getMessage());
		ctx.put("pattern", e.getPattern());
		return ctx;
	}

	/**
	 * Gets the locale that should be used for the current validation.
	 * @return the locale that should be used for the current validation
	 */
	public final Locale getLocaleForValidation()
	{
		Locale localeForValidation = null;
		if (isUseLocalized())
		{
			localeForValidation = getLocale();
			if (localeForValidation == null)
			{
				localeForValidation = RequestCycle.get().getSession().getLocale();
			}
		}
		return localeForValidation;
	}

	/**
	 * Gets the type to use for checking.
	 * @return the type to use for checking
	 */
	public final Class getType()
	{
		return type;
	}

	/**
	 * Gets whether to use either the set locale or the session's locale.
	 * @return whether to use either the set locale or the session's locale.
	 */
	public final boolean isUseLocalized()
	{
		return useLocalized;
	}

	/**
	 * Gets the locale to use. if null and useLocaled == true, the session's locale will be
	 * used..
	 * @return the locale to use
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer("TypeValidator{type=")
			.append(getType());
		if(isUseLocalized())
		{
			Locale loc = getLocale();
			if(loc != null)
			{
				b.append(",locale=").append(getLocale());
			}
			else
			{
				b.append(",(use client locale)");
			}
		}
		b.append("}");
		return b.toString();
	}
}