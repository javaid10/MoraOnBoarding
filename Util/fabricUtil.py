import subprocess
from win10toast import ToastNotifier
import requests

mfcliFilePath = "mfcli.jar"
mavenBuildPath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService"
jarFilePath = "D:\\MoraFinance\\WorkspaceMora\\moraJavaService\\target\\UserReg-0.0.1-SNAPSHOT.jar"
jarName = "UserReg-0.0.1-SNAPSHOT.jar"
targetJarName = "UserReg-0.0.1-SNAPSHOT.jar"
fabricURL = "https://api2.staging.morafinance.com:8443"
authURL = "https://api2.staging.morafinance.com:8443"
appName = "Origination"
fabricUser = "rajath@bank.com"
fabricPassword = "Kony@1234"
envName = "Dev"
toast = ToastNotifier()


def mavenBuild():
    print("Building app")
    # Change the directory to the one where the maven file is present
    subprocess.call(" mvn clean install", shell=True, cwd=mavenBuildPath)


def publishApp():
    print("Publishing app to fabric")
    subprocess.call("java -jar {6} Publish -u {0} -p {1} -e {2} -a {3} -au {4} -cu {5}".format(
        fabricUser, fabricPassword, envName, appName, fabricURL, authURL, mfcliFilePath), shell=True)
    toast.show_toast("Publish Completed",
                     "Build,Upload and publish completed successfully", duration=20, threaded=True)


def uploadJar():
    print("Uploading jar file  to fabric")
    subprocess.call("java -jar {0} import-Jar -u {1} -p {2} -au {3} -cu {4} -f {5} --override".format(
        mfcliFilePath, fabricUser, fabricPassword, fabricURL, authURL, jarFilePath, jarName), shell=True)


def sendsms():
  resp = requests.post('https://textbelt.com/text', {
    'phone': '+919206274135',
    'message': 'Hello Anitha you have won 5000000rs worth of gold please call 9206274135 for more information',
    'key': 'textbelt',})
  print(resp.json())

inp = input(
    " 1 Build Upload and Publish \n 2 Build and Upload Jar \n 3 Publish app \n ")

if inp == "1":
    mavenBuild()
    uploadJar()
    publishApp()
elif inp == "3":
    toast.show_toast("publishing app")
    publishApp()
    toast.show_toast("Publish Completed", duration=20, threaded=True)
elif inp == "2":
    toast.show_toast("build and upload app")
    mavenBuild()
    uploadJar()
    toast.show_toast("Uploaded Jar successfully", duration=20, threaded=True)
elif inp == "4":
    sendsms()
