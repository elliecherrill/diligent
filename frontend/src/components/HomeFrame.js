import {
    Button,
    Fade
} from '@material-ui/core'
import {
    KeyboardArrowDown as DownArrow
} from '@material-ui/icons'
import React from 'react'

const scrollToRef = (ref) => window.scrollTo({
    top: ref.current.offsetTop,
    left: 0,
    behavior: 'smooth'
})

const HomeFrame = ({title, subtitle, loaded, setGoToNewConfig, setGoToViewsConfigs,
                       downloadPlugin, includeHowTo, configs, howToRef}) => {

    return (
        <div style={{height: '95vh'}}>
            <section className='hero is-large'>
                <div className='hero-body'>
                    <div className='container'>
                        <Fade in={loaded}>
                            <h1 className='title is-1'
                                style={{fontSize: '5rem', textAlign: 'center', color: 'white'}}>
                                {title}
                            </h1>
                        </Fade>
                        <Fade in={loaded} {...(loaded ? {timeout: 1000} : {})}>
                            <h1 className='subtitle is-3'
                                style={{
                                    fontSize: '2rem',
                                    textAlign: 'center',
                                    color: 'rgb(220, 220, 220)',
                                    marginBottom: '5%'
                                }}>
                                {subtitle}
                            </h1>
                        </Fade>
                    </div>
                </div>
            </section>
            <Fade in={loaded} {...(loaded ? {timeout: 2000} : {})}>
                <div style={{display: 'flex', justifyContent: 'center', flexWrap: 'wrap'}}>
                    <Button
                        variant='outlined'
                        color='secondary'
                        onClick={() => setGoToNewConfig(true)}
                        style={{margin: '1.5%'}}
                    >
                        Create A New Configuration
                    </Button>
                    {configs !== null && configs.length > 0 &&
                    <Button
                        variant='outlined'
                        color='secondary'
                        onClick={() => setGoToViewsConfigs(true)}
                        style={{margin: '1.5%'}}
                    >
                        View Your Configurations
                    </Button>
                    }
                    <Button
                        variant='outlined'
                        color='secondary'
                        onClick={() => downloadPlugin()}
                        style={{margin: '1.5%'}}
                    >
                        Download IntelliJ Plugin
                    </Button>
                </div>
            </Fade>
            {includeHowTo &&
            <Fade in={loaded} {...(loaded ? {timeout: 3000} : {})}>
                <div style={{
                    position: 'absolute',
                    bottom: '3%',
                    display: 'flex',
                    justifyContent: 'center',
                    width: '100%'
                }}>
                    <Button
                        color='secondary'
                        onClick={() => scrollToRef(howToRef)}
                    >
                        <DownArrow/> How To Use Diligent <DownArrow/>
                    </Button>
                </div>
            </Fade>
            }

        </div>
    )
}


export default HomeFrame