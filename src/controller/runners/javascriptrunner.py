import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from runner import Runner
from selenium import webdriver
import asyncio

class JavaScriptRunner(Runner):
    def __init__(self, config):
        self.driver=webdriver.Chrome(executable_path='C:\Files\Tools\WebDrivers\chromedriver.exe')

    def setup(self):
        '''Setup testing environment.'''
        #driver = webdriver.Chrome(executable_path='D:\Program Files\WebDrivers\chromedriver.exe')


    async def run(self):
        '''Start to run tests.'''
        self.driver.get('http://localhost:8081/javascript/test.html')
        for entry in self.driver.get_log('browser'):
            print(entry)
        print('Setup')
        self.finish_event=asyncio.Event()
        await asyncio.wait([self.finish_event.wait()])

    def teardown(self):
        '''Completing testing. Free all resources.'''
        self.driver.get('http://localhost:8081/javascript/test.html')
        pass