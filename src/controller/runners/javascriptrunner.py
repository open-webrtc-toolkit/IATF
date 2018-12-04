import os, sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from runner import Runner
from selenium import webdriver

class JavaScriptRunner(Runner):
    def __init__(self, config):
        self.driver=webdriver.Chrome(executable_path='D:\Program Files\WebDrivers\chromedriver.exe')
        pass

    def setup(self):
        '''Setup testing environment.'''
        #driver = webdriver.Chrome(executable_path='D:\Program Files\WebDrivers\chromedriver.exe')
        self.driver.implicitly_wait(10) # seconds
        self.driver.get('http://localhost:8081/javascript/test.html')
        state_element = self.driver.find_element_by_id("state")
        for entry in self.driver.get_log('browser'):
            print(entry)
        print('Setup')

    def run(self):
        '''Start to run tests.'''
        self.driver.get('http://www.google.com')
        pass

    def teardown(self):
        '''Completing testing. Free all resources.'''
        self.driver.get('http://localhost:8081/javascript/test.html')
        pass