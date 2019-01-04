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
    def __init__(self, context):
        self.driver = webdriver.Chrome(
            executable_path='D:\Program Files\WebDrivers\chromedriver.exe')
        self.url = context.config['url']+'?taskId=' + \
            context.task_id+'&role='+context.role

    def setup(self):
        '''Setup testing environment.'''
        #driver = webdriver.Chrome(executable_path='D:\Program Files\WebDrivers\chromedriver.exe')

    def run(self):
        '''
        Start to run tests.
        It starts a browser specified, and waiting for an element with ID "iatf-state" to have text "Finished" in it.
        '''
        self.driver.get(self.url)
        try:
            WebDriverWait(self.driver, 30).until(
                EC.text_to_be_present_in_element((By.ID, 'iatf-state'), 'Finished'))
        except Exception:
            print('Exception')
        finally:
            self.driver.quit()

    def teardown(self):
        '''Completing testing. Free all resources.'''
        pass
