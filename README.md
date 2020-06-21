# Diligent
An automated analysis tool made by Helena Cherrill as part of Computing BEng, Imperial College London.

## Abstract
This project presents Diligent, an automated analysis tool that gives formative feedback to programmers as they code. The tool takes the form of an IDE plugin which has the capability to detect and give feedback on a range of issues, such as the violation of naming conventions, code duplication and superfluous keywords. In a rapidly growing developer population, the majority of people who learn to code are self-taught or use online courses. This results in them missing out on feedback from human tutors, which is crucial to learning how to solve a problem *well* rather than just how to solve a problem. Diligent aims to help improve this scenario by giving more than the correctness-based feedback which current program analysis tools focus on. Early experimental results show that Diligent achieves high accuracy detection of common programming issues.

## File Structure

 - `backend/` and `frontend/` contain the code written to create [https://diligent.doc.ic.ac.uk/](https://diligent.doc.ic.ac.uk/).
 - `inspections/` contains the code written to create the Diligent IntelliJ plugin (for Java).
 - `python-inspections/` contains the code written to create the Diligent for Python IntelliJ plugin.

## User Guide
This can also be found on the website [https://diligent.doc.ic.ac.uk/home](https://diligent.doc.ic.ac.uk/home)

**Step 1: Create Your Configuration**
The first step is to create a configuration which is where you select which checks are going to be performed for a particular IntelliJ project.  
  
From the Diligent home screen, select 'Create a New Configuration'. Drag and drop the checks to where you want them, add details (such as the course name) if you wish and finally, save your configuration.

**Step 2: Download Your Configuration**
To use your configuration, you need to download the configuration file (diligent.json). To do this, select 'View Your Configurations' from the Diligent home screen and click the 'Download Configuration File' button for the configuration you want to use.  
  
The diligent.json file which you downloaded must then be placed in the top level of your IntelliJ project. If no Diligent configuration file is found, the plugin will notify you and ask whether you want to use the default configuration.

**Step 3: Install the Diligent Plugin**
First, you will need to download the plugin. To do so, select 'Download IntelliJ Plugin' from the Diligent home screen.  
  
To install the plugin in IntelliJ on Windows / Ubuntu:  
-   From within IntelliJ, select File > Settings > Plugins
-   Then select the settings icon on the top bar and 'Install Plugin from Disk' then select the .zip file downloaded from the Diligent home screen.
-   Finally select OK, Restart IDE and Restart

  
To install the plugin in IntelliJ on Mac:  
-   From within IntelliJ, select IntelliJ IDEA > Preferences > Plugins
-   Then select the settings icon on the top bar and 'Install Plugin from Disk' then select the .zip file downloaded from the Diligent home screen.
-   Finally select OK, Restart IDE and Restart

Inspired By: https://github.com/nolequen/idea-inspections-plugin
