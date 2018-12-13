from threading import Timer
import asyncio
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium import webdriver
from selenium.webdriver.common.by import By
from runner import Runner
import os
import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


class JavaScriptRunner(Runner):
    def __init__(self, config):
        self.driver = webdriver.Chrome(
            executable_path='D:\Program Files\WebDrivers\chromedriver.exe')

    def setup(self):
        '''Setup testing environment.'''
        #driver = webdriver.Chrome(executable_path='D:\Program Files\WebDrivers\chromedriver.exe')

    async def run(self):
        '''Start to run tests.'''
        self.driver.get('http://localhost:8081/javascript/test.html')
        try:
            WebDriverWait(self.driver, 30).until(
                EC.text_to_be_present_in_element((By.ID, 'state'), 'Finished'))
        except Exception:
            print('Exception')
        finally:
            print('run')
            self.driver.quit()

    def teardown(self):
        '''Completing testing. Free all resources.'''
        self.driver.get('http://localhost:8081/javascript/test.html')
        pass
