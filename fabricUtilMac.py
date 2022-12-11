import subprocess
import requests
import pync
# mfcliFilePath = "mfcli.jar"
# mavenBuildPath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService"
# jarFilePath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService\\target\\UserReg-0.0.1-SNAPSHOT.jar"
# jarName = "UserReg-0.0.1-SNAPSHOT.jar"
# targetJarName = "UserReg-0.0.1-SNAPSHOT.jar"
# fabricURL = "https://api2.staging.morafinance.com:8443"
# authURL = "https://api2.staging.morafinance.com:8443"
# appName = "Origination"
# fabricUser = "rajath@bank.com"
# fabricPassword = "Kony@1234"
# envName = "Dev"
# toast = ToastNotifier()


# mfcliFilePath = "mfcli.jar"
# mavenBuildPath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService"
# jarFilePath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService\\target\\UserReg-0.0.1-SNAPSHOT.jar"
# jarName = "UserReg-0.0.1-SNAPSHOT.jar"
# targetJarName = "UserReg-0.0.1-SNAPSHOT.jar"
# fabricURL = "https://api2.staging.morafinance.com:8443"
# authURL = "https://api2.staging.morafinance.com:8443"
# appName = "Origination"
# fabricUser = "rajath@bank.com"
# fabricPassword = "Kony@1234"
# envName = "Dev"
# toast = ToastNotifier()mfcliFilePath = "mfcli.jar"
# mavenBuildPath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService"
# jarFilePath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService\\target\\UserReg-0.0.1-SNAPSHOT.jar"
# jarName = "UserReg-0.0.1-SNAPSHOT.jar"
# targetJarName = "UserReg-0.0.1-SNAPSHOT.jar"
# fabricURL = "https://api2.staging.morafinance.com:8443"
# authURL = "https://api2.staging.morafinance.com:8443"
# appName = "Origination"
# fabricUser = "rajath@bank.com"
# fabricPassword = "Kony@1234"
# envName = "Dev"
# toast = ToastNotifier()


mfcliFilePath = "/Users/rajathv/moraFinance/moraJavaService/Util/mfcli.jar"
mavenBuildPath = "/Users/rajathv/moraFinance/moraJavaService"
jarFilePath = "/Users/rajathv/moraFinance/moraJavaService/target/MoraOnboarding-0.0.1-SNAPSHOT.jar"
jarName = "MoraOnboarding-0.0.1-SNAPSHOT.jar"
targetJarName = "MoraOnboarding-0.0.1-SNAPSHOT.jar"
fabricURL = "https://api2.staging.morafinance.com:8443"
authURL = "https://api2.staging.morafinance.com:8443"
appName = "Origination"
fabricUser = "rajath@bank.com"
fabricPassword = "Kony@1234"
envName = "Dev"

# mfcliFilePath = "mfcli.jar"
# mavenBuildPath = "/Users/rajathv/moraFinance/moraJavaService"
# jarFilePath = "/Users/rajathv/moraFinance/moraJavaService/target/MoraOnboarding-0.0.1-SNAPSHOT.jar"
# jarName = "MoraOnboarding-0.0.1-SNAPSHOT.jar"
# targetJarName = "MoraOnboarding-0.0.1-SNAPSHOT.jar"
# fabricURL = "http://172.18.160.22:8080"
# authURL = "http://172.18.160.22:8080"
# appName = "Origination"
# fabricUser = "rajath@bank.com"
# fabricPassword = "Kony@1234"
# envName = "LocalDevEnv"
# toast = ToastNotifier()

def mavenBuild():
    print("Building app")
    # Change the directory to the one where the maven file is present
    subprocess.call(" mvn clean install", shell=True, cwd=mavenBuildPath)
    pync.notify('Build Completed', title='Fabric Automater')



def publishApp():
    print("Publishing app to fabric")
    subprocess.call("java -jar {6} Publish -u {0} -p {1} -e {2} -a {3} -au {4} -cu {5}".format(
        fabricUser, fabricPassword, envName, appName, fabricURL, authURL, mfcliFilePath), shell=True)
    


def uploadJar():
    print("Uploading jar file  to fabric")
    subprocess.call("java -jar {0} import-Jar -u {1} -p {2} -au {3} -cu {4} -f {5} --override".format(
        mfcliFilePath, fabricUser, fabricPassword, fabricURL, authURL, jarFilePath, jarName), shell=True)



inp = input(
    " 1 Build Upload and Publish \n 2 Build and Upload Jar \n 3 Publish app \n ")

if inp == "1":
    mavenBuild()
    uploadJar()
    publishApp()
elif inp == "3":
    publishApp()
elif inp == "2":
    mavenBuild()
    uploadJar()
elif inp == "4":
    sendsms()
