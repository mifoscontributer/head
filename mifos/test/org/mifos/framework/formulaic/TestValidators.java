package org.mifos.framework.formulaic;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.mifos.framework.util.helpers.DateUtils;

import junit.framework.TestCase;

public class TestValidators extends TestCase {
	
	private void checkException(Validator val, Object input, String error) {
		try {
			val.validate(input);
			fail();
		}
		catch (ValidationError e) {
			assertEquals(error, e.getMsg());
		}
		
	}
	
	public void testDateComponentValidator() throws Exception {
		Validator validator = new DateComponentValidator();
		Map<String, String> inputMap = new HashMap<String, String>();
		Date expectedDate = new DateMidnight(2000, 05, 20).toDate();
		inputMap.put("DD", "20");
		inputMap.put("MM", "5");
		inputMap.put("YY", "2000");
		assertEquals(expectedDate, validator.validate(inputMap));
		
		inputMap.put("MM", "20");
		checkException(validator, inputMap, DateValidator.DATE_FORMAT_ERROR);
		
		Schema schema = new Schema();
		schema.setMapValidator("mydate", validator);
		inputMap = new HashMap<String, String>();
		inputMap.put("mydate_DD", "20");
		inputMap.put("mydate_MM", "5");
		inputMap.put("mydate_YY", "2000");
		assertEquals(expectedDate, schema.validate(inputMap).get("mydate"));
		
	}
	
	public void testDateValidator() throws Exception {
		Validator validator = new DateValidator();
		Date expectedDate = new DateMidnight(2005, 04, 03).toDate();
		assertEquals(expectedDate, validator.validate("3/4/2005"));
		checkException(validator, "5/13/2000", DateValidator.DATE_FORMAT_ERROR);
	}
	
	public void testSchemaCompoundFunctionality() {
		Schema schema = new Schema();
		Map<String, String> data = new HashMap<String, String>();
		data.put("simple", "a");
		data.put("map_one", "b");
		data.put("map_two", "c");
		assertEquals("a", Schema.parseField("simple", Schema.FieldType.SIMPLE, data));
		assertNull(Schema.parseField("notafield", Schema.FieldType.SIMPLE, data));
		Map<String, Object> expectedResults = new HashMap<String, Object>();
		expectedResults.put("one", "b");
		expectedResults.put("two", "c");
		assertEquals(expectedResults, Schema.parseField("map", Schema.FieldType.MAP, data));
	}
	
	
	private enum TestEnum {
		VALUE_ONE,
	}
	
	public void testEnumValidator() throws Exception {
		Validator val = new EnumValidator(TestEnum.class);
		assertEquals(TestEnum.VALUE_ONE, val.validate("VALUE_ONE"));
		assertEquals(TestEnum.VALUE_ONE, val.validate("value_one"));
		checkException(val, null, BaseValidator.MISSING_ERROR);
		checkException(val, "bad value", EnumValidator.INVALID_ENUM_ERROR);
	}
	
	public void testSwitchValidator() throws Exception {
		SwitchValidator val = new SwitchValidator("switch_field");
		Schema schemaA = new Schema();
		schemaA.setSimpleValidator("age", new IntValidator());
		val.addCase("a", schemaA);
		Schema schemaB = new Schema();
		schemaB.setSimpleValidator("name", new MaxLengthValidator(5));
		val.addCase("b", schemaB);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("switch_field", "a");
		data.put("age", "2");
		Map<String, Object> results = val.validate(data);
		assertEquals(2, results.get("age"));
		
		data.put("name", "bob");
		data.put("switch_field", "b");
		results = val.validate(data);
		assertEquals("bob", results.get("name"));
		
		try {
			data.put("switch_field", "c");
			val.validate(data);
			fail();
		}
		catch (ValidationError e) {
			assertEquals(SwitchValidator.MISSING_EXPECTED_VALUE_ERROR, e.getMsg());
		}
		
		val.setDefaultCase(schemaB);
		val.validate(data);
	}
	
	public void testIntValidator() throws Exception {
		Validator val = new IntValidator();
		assertEquals(42, val.validate("42"));
		assertEquals(42, val.validate("042"));
		checkException(val, "notanumber", IntValidator.PARSE_ERROR);
		// check error when given wrong type
		checkException(val, new LinkedList(), IntValidator.WRONG_TYPE_ERROR);
		checkException(val, " 042", IntValidator.PARSE_ERROR);	
	}
	
	public void testIsInstanceValidator() throws Exception {
		Validator val = new IsInstanceValidator(Number.class);
		assertEquals(42, val.validate(42));
		assertEquals(new BigInteger("42"), val.validate(new BigInteger("42")));
		checkException(val, "a string", IsInstanceValidator.WRONG_TYPE_ERROR);
	}
	
	public void testIdentityValidator() throws Exception {
		Validator val = new IdentityValidator();
		String input = "a string";
		assertEquals(input, val.validate(input));
		assertEquals(null, val.validate(null));
	}
	
	public void testNullValidator() throws Exception {
		Validator val = new NullValidator(new IntValidator());
		assertEquals(null, val.validate(null));
		assertEquals(42, val.validate("42"));
		checkException(val, "a string", IntValidator.PARSE_ERROR);
	}
	
	public void testMaxLengthValidator() throws Exception {
		Validator val = new MaxLengthValidator(4);
		assertEquals("abc", val.validate("abc"));
		assertEquals("abcd", val.validate("abcd"));
		checkException(val, "toolong", MaxLengthValidator.TOO_LONG_ERROR);
	}
	
	public void testSchema() throws Exception {
		Schema schema = new Schema();
		schema.setSimpleValidator("fieldOne", new IntValidator());
		schema.setSimpleValidator("fieldTwo", new MaxLengthValidator(4));
		Map<String, String> input = new HashMap<String, String>();
		input.put("fieldOne", "42");
		input.put("fieldTwo", "abcd");
		Map<String, Object> results = schema.validate(input);
		assertTrue(42 == (Integer) results.get("fieldOne"));
		assertEquals("abcd", (String) results.get("fieldTwo"));
		
		input.put("fieldOne", null);
		input.put("fieldTwo", "abcde");
		try {
			schema.validate(input);
			fail();
		}
		catch (SchemaValidationError e) {
			assertEquals(2, e.size());
			assertEquals(MaxLengthValidator.TOO_LONG_ERROR, e.getFieldMsg("fieldTwo"));
			assertEquals(BaseValidator.MISSING_ERROR, e.getFieldMsg("fieldOne"));
		}
		// now make field one optional
		schema.setSimpleValidator("fieldOne", new NullValidator(new IntValidator()));
		try {
			schema.validate(input);
			fail();
		}
		catch(SchemaValidationError e) {
			assertEquals(1, e.size());
			assertEquals(MaxLengthValidator.TOO_LONG_ERROR, e.getFieldMsg("fieldTwo"));	
		}
	}
	
	// the itemsource version of OneOfValidator is intended to 
	// be used with inner classes (perhaps anonymous) like this..
	// so that instance methods to access hibernate persistence
	// will work
	private class TestItemSource implements OneOfValidatorSource {
		public Collection getItems() {
			LinkedList<String> items = new LinkedList<String>();
			items.add("one");
			items.add("two");
			return items;
		}
	}
	
	public void testOneOfValidator() throws Exception {
		LinkedList<String> items = new LinkedList<String>();
		items.add("one");
		items.add("two");
		Validator val = new OneOfValidator(items);
		assertEquals("one", val.validate("one"));
		checkException(val, null, OneOfValidator.MISSING_ERROR);
		checkException(val, "three", OneOfValidator.NOT_A_CHOICE_ERROR);
		
		val = new OneOfValidator(new TestItemSource());
		assertEquals("one", val.validate("one"));
		checkException(val, null, OneOfValidator.MISSING_ERROR);
		checkException(val, "three", OneOfValidator.NOT_A_CHOICE_ERROR);
	}
}
