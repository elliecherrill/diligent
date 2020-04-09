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
                {/*TODO: Write this*/}
                Diligent is ..., and this is how you create a configuration, download the configuration and install the plugin.
            </p>

            {/*STEP ONE: CREATE*/}
            <h1
                className='subtitle is-2'
                style={{fontSize: '2rem', fontWeight: '500', textAlign: 'left', color: theme.palette.primary.main}}
            >
                Step One: Create Your Configuration
            </h1>
            <p style={{fontSize: 'larger'}}>
                {/*TODO: Write this*/}
                The first step is to create a configuration ...
            </p>
            <div style={{display: 'flex', justifyContent: 'center'}}>
                <video loop autoPlay muted style={{width: '65%'}}>
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
                {/*TODO: Write this*/}
                To use your configuration, you need to download the configuration file (diligent.json) and place this in the top level of your Java project.
            </p>
            <div style={{display: 'flex', justifyContent: 'center'}}>
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
                {/*TODO: Write this*/}
                {/*TODO think about Linux vs Windows vs Mac*/}
                This is how you install the Diligent plugin into IntelliJ (or can get from marketplace...)
            </p>
            <div style={{display: 'flex', justifyContent: 'center'}}>
                <video loop autoPlay muted style={{width: '65%'}}>
                    <source src={useDiligent} type='video/mp4'/>
                    Your browser does not support the video tag.
                </video>
            </div>
        </section>
    )
}

export default HowTo