package com.mora.javaservice;
import com.temenos.infinity.api.commons.encrypt.BCrypt;
public class TestPasswd {

	public static void main(String[] args) {
		String dbPassword ="$2a$11$1nmtywkutpQtUgQOtVhwAOAzgyt1mxD279NKGuX8OFTbzOKIHF4wC";
		String currentPassword ="123456";
		boolean isPasswordValid = false;
		try {
			isPasswordValid = BCrypt.checkpw(currentPassword,dbPassword);

		} catch (Exception exception) {
		
			throw exception;
		}
		System.out.println(isPasswordValid);

	}

}
