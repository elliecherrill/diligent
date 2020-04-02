import {
    Button
} from '@material-ui/core'
import React, {useEffect, useState} from 'react'
import colours from '../../../constants/colours'
import {Redirect} from 'react-router-dom'
import routes from '../../../constants/routes'
import NewConfigSnackbar from "./NewConfigSnackbar";
import * as API from "../../../api";

const Home = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    const [goToNewConfig, setGoToNewConfig] = useState(false)
    const [goToViewConfigs, setGoToViewsConfigs] = useState(false)
    const [configs, setConfigs] = useState(null)

    useEffect(() => {
        API.get_my_configs().then(r => setConfigs(r))
    }, [])

    return (
        <div>
            <section className='hero is-large'>
                <div className='hero-body'>
                    <div className='container'>
                        <h1 className='title is-1' style={{fontSize: '5rem', textAlign: 'center', color: 'white'}}>
                            Diligent
                        </h1>
                        <h1 className='subtitle is-3'
                            style={{
                                fontSize: '2rem',
                                textAlign: 'center',
                                color: 'rgb(220, 220, 220)',
                                marginBottom: '5%'
                            }}>
                            A tool which does something.
                        </h1>
                    </div>
                </div>
            </section>
            <div style={{display: 'flex', justifyContent: 'center'}}>
                <Button
                    variant='outlined'
                    color='secondary'
                    onClick={() => setGoToNewConfig(true)}
                    style={{marginRight: '1.5%'}}
                >
                    Create A New Configuration
                </Button>
                <Button
                    variant='outlined'
                    color='secondary'
                    disabled={!configs || configs.length === 0}
                    onClick={() => setGoToViewsConfigs(true)}
                    style={{marginLeft: '1.5%'}}
                >
                    View Your Configurations
                </Button>
            </div>

            {goToNewConfig ? <Redirect push to={routes.NEW_CONFIG}/> : false}
            {goToViewConfigs ? <Redirect push to={routes.VIEW_CONFIGS}/> : false}
            {props.location.state ? <NewConfigSnackbar title={props.location.state} setGoToViewsConfigs={setGoToViewsConfigs} /> : false}
        }
        </div>
    )
}


export default Home