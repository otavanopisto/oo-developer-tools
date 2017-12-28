#!/bin/sh
wget https://chromedriver.storage.googleapis.com/2.34/chromedriver_linux64.zip
unzip chromedriver_linux64.zip
sudo cp chromedriver /usr/bin/chromedriver
sudo chown root /usr/bin/chromedriver
sudo chmod +x /usr/bin/chromedriver
sudo chmod 755 /usr/bin/chromedriver
rm chromedriver_linux64.zip chromedriver
echo "All Done!"

