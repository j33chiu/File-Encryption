package main;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

public class AES {

	private byte[] key = new byte[16];
	private SecureRandom secureRandom;
	
	public AES(byte[] key) {
		for(int i = 0; i < key.length; i++) {
			this.key[i] = key[i];
		}
		secureRandom = new SecureRandom();
	}
	
	public AES(){
		secureRandom = new SecureRandom();
		secureRandom.nextBytes(key);
	}
	
	public byte[] encrypt(byte[] plainText) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, "AES");
			
			byte[] iv = new byte[12];
			secureRandom.nextBytes(iv);
			
			final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			
			/*
			if(associatedData != null) {
				cipher.updateAAD(associatedData);
			}*/
			
			byte[] cipherText = cipher.doFinal(plainText);
			
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
			byteBuffer.putInt(iv.length);
			byteBuffer.put(iv);
			byteBuffer.put(cipherText);
			
			byte[] cipherMessage = byteBuffer.array();
			
			Arrays.fill(plainText, (byte)0);
			Arrays.fill(iv, (byte)0);
			Arrays.fill(cipherText, (byte)0);
			
			return cipherMessage;
		}catch(NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
			e.printStackTrace();
			return new byte[0];
		}
	}
	

	public byte[] decrypt(byte[] cipherMessage) {
		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
			int ivLength = byteBuffer.getInt();
			if(ivLength < 12 || ivLength >= 16) { // check input parameter
				JOptionPane.showMessageDialog(null, "Error occured when decrypting", "Error", JOptionPane.ERROR_MESSAGE);
			    throw new IllegalArgumentException("invalid iv length");
			}
			byte[] iv = new byte[ivLength];
			byteBuffer.get(iv);
			byte[] cipherText = new byte[byteBuffer.remaining()];
			byteBuffer.get(cipherText);
			
			final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));//128
			/*if (associatedData != null) {
			    cipher.updateAAD(associatedData);
			}*/
			byte[] plainText= cipher.doFinal(cipherText);
			
			Arrays.fill(cipherMessage, (byte)0);
			Arrays.fill(cipherText, (byte)0);
			Arrays.fill(iv, (byte)0);
			
			return plainText;
		}
		catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			System.out.println("oops");
			return new byte[0];
		}
	}
	
	public void keyErase() {
		Arrays.fill(this.key, (byte)0);
	}
}

