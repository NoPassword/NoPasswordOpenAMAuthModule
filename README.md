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

![image alt text](/images/api_key.png)

5. Login into AM console as an administrator and go to `Realms > Top Level Real > Authentication > Modules`.
6. Click on **Add Module** button. Name the module NoPassword and select NoPassword module from the Type list.

![image](/images/add_module_1.png)

7. Set **NoPassword Login Key**. Paste you NoPassword Login key from step 4 here.

![image alt text](/images/add_module_2.png)

8. Set **Authentication URL** with `https://api.nopassword.com/auth/login`. Save changes.
9. You can test the NoPassword authentication module by accessing this URL in your browser `https://YOUR_AM_SERVER_HERE/openam/XUI/?realm=/#login/&module=NoPassword`.</br>
10. Enter your username and hit enter. NoPassword AM Module will search for user email (mail or email attribute) in the data store if email is empty an email address will be generated from user DN. An authentication request will be send to NoPassword through the AM module. NoPassword will verify you username and key. If everything is correct you should get an authentication request on your phone.

![image](/images/demo_auth.png)