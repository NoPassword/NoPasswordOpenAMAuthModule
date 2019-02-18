![image alt text](/images/nopassword_logo.png)

# NoPassword Authentication Module

The NoPassword Authentication Module allows ForgeRock users to integrate their AM instance to the NoPassword authentication services.
This document assumes that you already have an AM 5.5+ instance running with an users base configured.

## Installation

Follow this steps in order to install the module:

1. Download the jar file from [here](target/nopassword-openam-auth-module-1.0.zip).
2. Copy the **nopassword-openam-auth-module-1.0.jar** file on your server: `/path/to/tomcat/webapps/openam/WEB-INF/lib`
3. Restart AM.
4. Login into NoPassword admin portal and open the `Keys` menu on the left side. Copy the **NoPassword Login** key value by clicking in the green button and save it for later.
5. If you wish to enable user provisioning to NoPassword, then copy **Generic API Key**, and download the RSA public key. You'll the RSA private key too.

![image alt text](/images/api_key.png)

6. Login into AM console as an administrator and go to `Realms > Top Level Real > Authentication > Modules`.
7. Click on **Add Module** button. Name the module NoPassword and select NoPassword module from the Type list.

![image](/images/add_module_1.png)

8. Set **NoPassword Login Key**. Paste you NoPassword Login key from step 4 here.

![image alt text](/images/add_module_2.png)

9. Set **Authentication URL** with `https://api.nopassword.com/auth/login`. You can leave the other fileds empty if you don't plan to enable user provisioning. Save changes.
10. Set the following values from step 5 to enable user provisioning:
- **Generic API Key**
- **RSA Private Key**
- **RSA Public Key**
- **Auth. Module URL**: You need a working AM authentication module to authenticate users locally prior to be registered at NoPassword. Set this URL replacing the required values (AM server and module's name) http://**YOUR_AM_SERVER_HERE**/openam/json/realms/root/authenticate?module=**MODULE_NAME**&authIndexType=module&authIndexValue=**MODULE_NAME**

11. You can test the NoPassword authentication module by accessing this URL in your browser `https://**YOUR_AM_SERVER_HERE**/openam/XUI/?realm=/#login/&module=NoPassword`.</br>
12. Enter your username and hit enter. NoPassword AM Module will search for user email (mail or email attribute) in the data store if email is empty an email address will be generated from user DN. An authentication request will be send to NoPassword through the AM module. NoPassword will verify you username and key. If everything is correct you should get an authentication request on your phone.

![image](/images/demo_auth.png)