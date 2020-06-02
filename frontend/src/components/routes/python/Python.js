import {
    Grid,
    CircularProgress,
} from '@material-ui/core'
import React, {useEffect, useState} from 'react'
import colours from '../../../constants/colours'
import HomeFrame from '../../HomeFrame'
import * as API from '../../../api'
import routes from '../../../constants/routes'
import NewConfigSnackbar from '../home/NewConfigSnackbar'
import EditConfigSnackbar from '../home/EditConfigSnackbar'
import {Redirect} from 'react-router-dom'

const Python = (props) => {
    document.body.style.backgroundColor = colours.PRIMARY
    props.setGoToPython(false)

    const [loaded, setLoaded] = useState(false)
    const [goToNewConfig, setGoToNewConfig] = useState(false)
    const [goToViewConfigs, setGoToViewsConfigs] = useState(false)
    const [configs, setConfigs] = useState(null)

    useEffect(() => {
        API.get_my_configs('python').then(r => {
            setConfigs(r)
            setLoaded(true)
        })
    }, [])

    const downloadPlugin = () => {
        API.get_python_plugin().then(r => {
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
                    <CircularProgress color={'secondary'}/>
                </Grid>
            </Grid>
        </div>)
    }

    return (
        <div>
            <HomeFrame
                title={'Diligent for Python'}
                subtitle={'Improving your coding practices in Python'}
                loaded={loaded}
                setGoToNewConfig={setGoToNewConfig}
                setGoToViewsConfigs={setGoToViewsConfigs}
                downloadPlugin={downloadPlugin}
                configs={configs}
                includeHowTo={false}
            />

            {goToNewConfig && <Redirect push to={routes.NEW_PYTHON_CONFIG}/>}
            {goToViewConfigs && <Redirect push to={routes.VIEW_PYTHON_CONFIGS}/>}
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


export default Python