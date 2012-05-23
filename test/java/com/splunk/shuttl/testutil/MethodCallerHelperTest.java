package com.splunk.shuttl.testutil;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

@Test(groups = { "fast-unit" })
public class MethodCallerHelperTest {

    @Test(groups = { "fast-unit" })
    public void getCallerToMyMethod_should_return_thisClass_when_invokingCaller_call() {
	Class<?> clazz = new Caller().call();
	assertEquals(clazz, getClass());
    }

    @Test(groups = { "fast-unit" })
    public void getCallerToMyMethod_should_stillGetThisClass_when_otherInternalCallsInsideTheClassIsCalled() {
	Class<?> clazz = new Caller().internalCall_before_call();
	assertEquals(clazz, getClass());
    }

    @Test(groups = { "fast-unit" }, expectedExceptions = { IllegalStateException.class })
    public void should_throw_IllegalStateException_when_stackTrace_containsOnlyTheSameClass() {
	StackTraceElement[] elements = getElementsWithSameClass();
	MethodCallerHelper.getCallerToMyMethod(elements);
    }

    @Test(groups = { "fast-unit" })
    public void testExtendedClassByCallingOverriddenMethod_shouldReturnCallerChild() {
	Class<?> clazz = new CallerChild().call();
	assertEquals(clazz, getClass());
    }

    // /**
    // * This test don't work yet and maybe it doesn't have to (currently only
    // * used by some classes in {@link com.splunk.shuttl.testutil}).
    // */
    // @Test(groups = { "fast-unit" })
    // public void
    // testExtendedClassByCallingCircularSuperCall_shouldReturnCallerChild() {
    // Class<?> clazz = new CallerChild().circularSuperCall();
    // assertEquals(clazz, getClass());
    // }

    private StackTraceElement[] getElementsWithSameClass() {
	StackTraceElement[] elements = new StackTraceElement[MethodCallerHelper.INDEX_OF_CALLER_TO_THIS_METHOD + 1];
	for (int i = 0; i < elements.length; i++)
	    elements[i] = new StackTraceElement("class", "methodName",
		    "fileName", 0);
	return elements;
    }

    private static class Caller {

	public Class<?> internalCall_before_call() {
	    return internalCall();
	}

	public Class<?> internalCall() {
	    return call();
	}

	public Class<?> call() {
	    return MethodCallerHelper.getCallerToMyMethod();
	}
    }

    private static class CallerChild extends Caller {

	@Override
	public Class<?> call() {
	    return MethodCallerHelper.getCallerToMyMethod();
	}

	@SuppressWarnings("unused")
	public Class<?> circularSuperCall() {
	    return internalCall();
	}

    }
}