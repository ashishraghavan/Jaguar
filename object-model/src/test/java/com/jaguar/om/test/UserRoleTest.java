package com.jaguar.om.test;

import org.testng.annotations.Test;

@Test(groups = "user_role",dependsOnGroups = {"user","role"})
public class UserRoleTest extends BaseTestCase {

}
