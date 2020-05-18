import {
    Button,
    Grid,
    CircularProgress,
    Fade
} from '@material-ui/core'
import React, {useEffect, useState, useRef} from 'react'
import colours from '../../../constants/colours'
import {Redirect} from 'react-router-dom'
import routes from '../../../constants/routes'
import NewConfigSnackbar from './NewConfigSnackbar'
import * as API from '../../../api'
import EditConfigSnackbar from './EditConfigSnackbar'
import HowTo from './HowTo'
import {
    KeyboardArrowDown as DownArrow
} from '@material-ui/icons'

const scrollToRef = (ref) => window.scrollTo({
    top: ref.current.offsetTop,
    left: 0,
    behavior: 'smooth'
})


const Home = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    props.setGoToHome(false)

    const [goToNewConfig, setGoToNewConfig] = useState(false)
    const [goToViewConfigs, setGoToViewsConfigs] = useState(false)
    const [configs, setConfigs] = useState(null)
    const [loaded, setLoaded] = useState(false)
    const howTo = useRef(null)

    useEffect(() => {
        API.get_my_configs().then(r => {
            setConfigs(r)
            setLoaded(true)
        })
    }, [])

    const downloadPlugin = () => {
        API.get_plugin().then(r => {
            if (r) {
                console.log('SUCCESS')
            } else {
                console.log('FAILURE')
            }
        })
    }

    if (!loaded) {
        return (<div>
            <Grid container justify='center'>
                <Grid item>
                    <CircularProgress color={"secondary"}/>
                </Grid>
            </Grid>
        </div>)
    }

    return (
        <div>
            <div style={{height: '95vh'}}>
                <section className='hero is-large'>
                    <div className='hero-body'>
                        <div className='container'>
                            <Fade in={loaded}>
                                <h1 className='title is-1'
                                    style={{fontSize: '5rem', textAlign: 'center', color: 'white'}}>
                                    Diligent
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
                                    Improving your coding practices
                                </h1>
                            </Fade>
                        </div>
                    </div>
                </section>
                <Fade in={loaded} {...(loaded ? {timeout: 2000} : {})}>
                    <div style={{display: 'flex', justifyContent: 'center'}}>
                        <Button
                            variant='outlined'
                            color='secondary'
                            onClick={() => setGoToNewConfig(true)}
                            style={{marginRight: '1.5%'}}
                        >
                            Create A New Configuration
                        </Button>
                        {configs !== null && configs.length > 0 &&
                        <Button
                            variant='outlined'
                            color='secondary'
                            onClick={() => setGoToViewsConfigs(true)}
                            style={{marginLeft: '1.5%', marginRight: '1.5%'}}
                        >
                            View Your Configurations
                        </Button>
                        }
                        <Button
                            variant='outlined'
                            color='secondary'
                            onClick={() => downloadPlugin()}
                            style={{marginLeft: '1.5%'}}
                        >
                            Download IntelliJ Plugin
                        </Button>
                    </div>
                </Fade>
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
                            onClick={() => scrollToRef(howTo)}
                        >
                            <DownArrow/> How To Use Diligent <DownArrow/>
                        </Button>
                    </div>
                </Fade>
            </div>

            <div ref={howTo} style={{height: '100%'}}>
                <HowTo/>
            </div>


            {goToNewConfig && <Redirect push to={routes.NEW_CONFIG}/>}
            {goToViewConfigs && <Redirect push to={routes.VIEW_CONFIGS}/>}
            {props.location.state &&
            (props.location.state.new ?
                    <NewConfigSnackbar
                        key={props.location.state.title}
                        title={props.location.state.title}
                        setGoToViewsConfigs={setGoToViewsConfigs}
                    />
                    :
                    (props.location.state.edit &&
                        <EditConfigSnackbar
                            key={props.location.state.title}
                            title={props.location.state.title}
                            setGoToViewsConfigs={setGoToViewsConfigs}
                        />
                    )
            )}
            }
        </div>
    )
}


export default Home