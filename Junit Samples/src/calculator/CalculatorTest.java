package calculator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import runner.NestedRunner;
import runner.TestCategory;

@RunWith(NestedRunner.class)
public class CalculatorTest {

	@TestCategory
	public static class GeneralTests {

		@Test
		public void evaluatesExpression() {
			Calculator calculator = new Calculator();
			int sum = calculator.evaluate("1+2+3");
			assertEquals(6, sum);
		}

		@Test
		public void evaluatesExpressionNeg() {
			Calculator calculator = new Calculator();
			int sum = calculator.evaluate("123");
			assertEquals(-4, sum);
		}

		@TestCategory
		public static class GeneralSubTests {

			@Test
			public void evaluatesExpressionSub() {
				Calculator calculator = new Calculator();
				int sum = calculator.evaluate("1+2+3");
				assertEquals(6, sum);
			}

			@Test
			public void evaluatesExpressionNegSub() {
				Calculator calculator = new Calculator();
				int sum = calculator.evaluate("123");
				assertEquals(-4, sum);
			}

		}

	}

	@TestCategory
	public static class SpecialTests {

		@Test
		public void evaluatesExpressionTwo() {
			Calculator calculator = new Calculator();
			int sum = calculator.evaluate("1+2+3+3");
			assertEquals(9, sum);
		}

		@Test
		public void evaluatesExpressionInvalid() {
			Calculator calculator = new Calculator();
			int sum = calculator.evaluate("X");
			assertEquals(3, sum);
		}
	}
}