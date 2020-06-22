import theme from '../../../theme'
import React from 'react'
import createConfig from '../../../images/create-config.mp4'
import downloadConfig from '../../../images/download-config.png'
import useDiligent from '../../../images/use-diligent.mp4'

const HowTo = () => {
    return (
        <section
            className='hero is-large'
            style={{
                backgroundColor: 'white',
                height: '100%',
                paddingTop: '2.5%',
                paddingBottom: '5%',
                paddingLeft: '5%',
                paddingRight: '5%'
            }}
        >
            <h1
                className='title is-1'
                style={{fontSize: '3rem', textAlign: 'left', color: theme.palette.primary.main}}
            >
                How To Use Diligent
            </h1>
            <p style={{fontSize: 'larger'}}>
                Diligent is an IntelliJ plugin which helps you to improve your coding practices. The steps below detail
                how to create a configuration, download a configuration and install the plugin.
            </p>

            {/*STEP ONE: CREATE*/}
            <h1
                className='subtitle is-2'
                style={{fontSize: '2rem', fontWeight: '500', textAlign: 'left', color: theme.palette.primary.main}}
            >
                Step One: Create Your Configuration
            </h1>
            <p style={{fontSize: 'larger'}}>
                The first step is to create a configuration which is where you select which checks are going to be
                performed for a particular IntelliJ project. <br/> <br/>
                From the Diligent home screen, select 'Create a New Configuration'. Drag and drop the checks to where
                you want them, add details (such as the course name) if you wish and finally, save your configuration.
            </p>
            <div style={{textAlign: 'center'}}>
                <video loop autoPlay muted playsinline style={{width: '65%'}}>
                    <source src={createConfig} type='video/mp4'/>
                    Your browser does not support the video tag.
                </video>
            </div>

            {/*STEP TWO: DOWNLOAD*/}
            <h1
                className='subtitle is-2'
                style={{fontSize: '2rem', fontWeight: '500', textAlign: 'left', color: theme.palette.primary.main}}
            >
                Step Two: Download Your Configuration
            </h1>
            <p style={{fontSize: 'larger'}}>
                To use your configuration, you need to download the configuration file (diligent.json). To do this,
                select
                'View Your Configurations' from the Diligent home screen and click the 'Download Configuration File'
                button for the configuration you want to use. <br/> <br/>
                The diligent.json file which you downloaded must then be placed in the top level of your IntelliJ project.
                If no Diligent configuration file is found, the plugin will notify you and ask whether you want to use the default configuration.
            </p>
            <div style={{textAlign: 'center'}}>
                <img src={downloadConfig} style={{width: '65%'}} alt={'how to download the config file'}/>
            </div>

            {/*STEP THREE: USE*/}
            <h1
                className='subtitle is-2'
                style={{fontSize: '2rem', fontWeight: '500', textAlign: 'left', color: theme.palette.primary.main}}
            >
                Step Three: Install the Diligent Plugin
            </h1>
            <p style={{fontSize: 'larger'}}>
                First, you will need to download the plugin. To do so, select 'Download IntelliJ Plugin' from the Diligent home screen. <br/> <br/>
                To install the plugin in IntelliJ on Windows / Ubuntu: <br/>
                <ul>
                    <li>From within IntelliJ, select File > Settings > Plugins</li>
                    <li>Then select the settings icon on the top bar and 'Install Plugin from Disk' then select the .zip file downloaded from the Diligent home screen. </li>
                    <li>Finally select OK, Restart IDE and Restart </li>
                </ul>
                <br/>
                To install the plugin in IntelliJ on Mac: <br/>
                <ul>
                    <li>From within IntelliJ, select IntelliJ IDEA > Preferences > Plugins</li>
                    <li>Then select the settings icon on the top bar and 'Install Plugin from Disk' then select the .zip file downloaded from the Diligent home screen. </li>
                    <li>Finally select OK, Restart IDE and Restart </li>
                </ul>
            </p>
            <div style={{textAlign: 'center'}}>
                <video loop autoPlay muted playsinline style={{width: '65%'}}>
                    <source src={useDiligent} type='video/mp4'/>
                    Your browser does not support the video tag.
                </video>
            </div>
        </section>
    )
}

export default HowTo