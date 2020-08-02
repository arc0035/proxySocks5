package com.cyz.socks5.server.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 密码认证方式. version||usernameLength||username||passwordlength||password
 */
public class UserPasswordAuthenticationRequest implements SocksMessage {

    /**
     * 认证子协商版本。和socks的0x05无关
     */
    private byte version;

    /**
     * 用户名长度
     */
    private byte usernameLength;

    private String username;

    private byte passwordLength;

    private String password;


    @Override
    public void serialize(OutputStream os) throws IOException {
        os.write(this.version);
        os.write(usernameLength);
        os.write(username.getBytes());
        os.write(passwordLength);
        os.write(password.getBytes());
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        int version = is.read();
        int usernameLength = is.read();
        byte[] username = read(is, usernameLength);
        int passwordLength = is.read();
        byte[] password = read(is, passwordLength);

        this.version = (byte)version;
        this.usernameLength = (byte)usernameLength;
        this.username = new String(username);
        this.passwordLength = (byte)passwordLength;
        this.password = new String(password);
    }

    private byte[] read(InputStream is, int length) throws IOException{
        byte[] result = new byte[length];
        int nread = 0;
        while (nread < length){
            nread += is.read(result, nread, length - nread);
        }
        return result;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getUsernameLength() {
        return usernameLength;
    }

    public void setUsernameLength(byte usernameLength) {
        this.usernameLength = usernameLength;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(byte passwordLength) {
        this.passwordLength = passwordLength;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
