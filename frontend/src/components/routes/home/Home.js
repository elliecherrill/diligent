import {
    Grid,
    CircularProgress,
} from '@material-ui/core'
import React, {useEffect, useState, useRef} from 'react'
import colours from '../../../constants/colours'
import {Redirect} from 'react-router-dom'
import routes from '../../../constants/routes'
import NewConfigSnackbar from './NewConfigSnackbar'
import * as API from '../../../api'
import EditConfigSnackbar from './EditConfigSnackbar'
import HowTo from './HowTo'
import HomeFrame from "../../HomeFrame";

const Home = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    props.setGoToHome(false)

    const [goToNewConfig, setGoToNewConfig] = useState(false)
    const [goToViewConfigs, setGoToViewsConfigs] = useState(false)
    const [configs, setConfigs] = useState(null)
    const [loaded, setLoaded] = useState(false)
    const howTo = useRef(null)

    useEffect(() => {
        API.get_my_configs('java').then(r => {
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
            <HomeFrame
                title={"Diligent"}
                subtitle={"Improving your coding practices"}
                loaded={loaded}
                setGoToNewConfig={setGoToNewConfig}
                setGoToViewsConfigs={setGoToViewsConfigs}
                downloadPlugin={downloadPlugin}
                includeHowTo={true}
                configs={configs}
                howToRef={howTo}
            />

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