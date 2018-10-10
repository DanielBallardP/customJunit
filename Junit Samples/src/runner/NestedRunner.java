package runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;

/**
 * A custom jUnit Test runner which allows to group & nest tests by inner test
 * classes annotated with @TestCategory. Additionally the test categories & test
 * descriptions are displayed in a more readable way.
 * 
 * @author: DPF
 * @since: 17.00
 */
public class NestedRunner extends Suite {

	private Class testClass;
	private HashMap<Method, Description> methodDescriptions;

	public NestedRunner(Class<?> testClass, RunnerBuilder builder) throws Throwable {
		super(builder, testClass, filterSubClasses(testClass.getClasses()));
		this.testClass = testClass;
		methodDescriptions = new HashMap<>();
	}

	private static Class<?>[] filterSubClasses(final Class<?>[] classes) {
		final List<Class<?>> filteredList = new ArrayList<Class<?>>();
		final List<Class<?>> subFilteredList = new ArrayList<Class<?>>();

		for (final Class<?> testClass : classes) {
			if (!Modifier.isAbstract(testClass.getModifiers()) && testClass.getAnnotation(TestCategory.class) != null) {
				if (testClass.getClasses() != null) {
					for (final Class<?> subTestClass : testClass.getClasses()) {
						if (!Modifier.isAbstract(testClass.getModifiers())
								&& testClass.getAnnotation(TestCategory.class) != null) {
							subFilteredList.add(subTestClass);
						}
					}

				}
				filteredList.add(testClass);
			}
		}

		return filteredList.toArray(new Class<?>[filteredList.size()]);
	}

	@Override
	public Description getDescription() {
		Description description = Description.createSuiteDescription(testClass.getSimpleName(),
				testClass.getAnnotations());

		for (Class<?> subClass : testClass.getClasses()) {
			Annotation annotation = subClass.getAnnotation(TestCategory.class);
			Description subClassDescription = Description.EMPTY;
			if (annotation != null) {
				subClassDescription = Description.createSuiteDescription(subClass.getSimpleName(), annotation);
			}

			if (subClass.getMethods() != null) {
				for (Method method : subClass.getMethods()) {
					Annotation methodAnnotation = method.getAnnotation(Test.class);
					if (methodAnnotation != null) {
						Description methodDescription = Description.createTestDescription(subClass.getSimpleName(),
								method.getName(), methodAnnotation);
						subClassDescription.addChild(methodDescription);
						methodDescriptions.put(method, methodDescription);
					}
				}
			}
			description.addChild(subClassDescription);

			if (subClass.getClasses() != null) {
				for (Class<?> subTestClass : subClass.getClasses()) {
					Annotation subAnnotation = subTestClass.getAnnotation(TestCategory.class);
					Description subTestClassDescription = Description.EMPTY;
					if (annotation != null) {
						subTestClassDescription = Description.createSuiteDescription(subTestClass.getSimpleName(),
								subAnnotation);
					}

					if (subTestClass.getMethods() != null) {
						for (Method method : subTestClass.getMethods()) {
							Annotation methodAnnotation = method.getAnnotation(Test.class);
							if (methodAnnotation != null) {
								Description methodDescription = Description.createTestDescription(
										subTestClass.getSimpleName(), method.getName(), methodAnnotation);
								subTestClassDescription.addChild(methodDescription);
								methodDescriptions.put(method, methodDescription);
							}
						}
					}
					description.addChild(subTestClassDescription);
				}
			}
		}

		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		try {
			for (Class<?> subClass : testClass.getClasses()) {
				if (subClass.isAnnotationPresent(TestCategory.class)) {
					Object testObject = subClass.newInstance();
					if (subClass.getMethods() != null) {
						for (Method method : subClass.getMethods()) {
							if (method.isAnnotationPresent(Test.class)) {
								Description methodDescription = Description.createTestDescription(
										subClass.getSimpleName(), method.getName(), method.getAnnotation(Test.class));
								notifier.fireTestStarted(methodDescription);
								try {
									method.invoke(testObject);
								} catch (InvocationTargetException | AssertionError e) {
									Failure failure = new Failure(methodDescription, e);
									notifier.fireTestFailure(failure);
								}
								notifier.fireTestFinished(Description.createTestDescription(subClass.getSimpleName(),
										method.getName(), method.getAnnotation(Test.class)));

							}
						}
					}

					if (subClass.getClasses() != null) {
						for (Class<?> subTestClass : subClass.getClasses()) {
							if (subClass.isAnnotationPresent(TestCategory.class)) {
								Object subTestObject = subTestClass.newInstance();
								if (subClass.getMethods() != null) {
									for (Method method : subTestClass.getMethods()) {
										if (method.isAnnotationPresent(Test.class)) {
											Description methodDescription = Description.createTestDescription(
													subTestClass.getSimpleName(), method.getName(),
													method.getAnnotation(Test.class));
											notifier.fireTestStarted(methodDescription);
											try {
												method.invoke(subTestObject);
											} catch (InvocationTargetException | AssertionError e) {
												Failure failure = new Failure(methodDescription, e);
												notifier.fireTestFailure(failure);
											}
											notifier.fireTestFinished(
													Description.createTestDescription(subTestClass.getSimpleName(),
															method.getName(), method.getAnnotation(Test.class)));

										}
									}
								}
							}
						}
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}