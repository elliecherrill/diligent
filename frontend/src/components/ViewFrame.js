import React, {useState, useEffect} from 'react'
import colours from '../constants/colours'
import styled from 'styled-components'
import useStyles from './routes/viewConfigs/style'
import * as API from '../api'
import {
    Grid,
    CircularProgress,
    Slide
} from '@material-ui/core'
import Alert from './routes/viewConfigs/Alert'
import DeleteConfigSnackbar from './routes/viewConfigs/DeleteConfigSnackbar'
import ConfigTable from './routes/viewConfigs/ConfigTable'

const Container = styled.div`
    margin: 7%;    
    display: flex;
    flex-direction: column;
`

const ViewFrame = ({type, createFile, initialConfigs, editRoute}) => {
    document.body.style.backgroundColor = colours.PRIMARY

    const classes = useStyles()
    const [configs, setConfigs] = useState(null)
    const [openDeleteAlert, setOpenDeleteAlert] = useState(false)
    const [currentConfig, setCurrentConfig] = useState(null)
    const [refresh, setRefresh] = useState(false)
    const [deleted, setDeleted] = useState(false)

    useEffect(() => {
        API.get_my_configs(type).then(r => {
            setConfigs(r)
            setRefresh(false)
        })
    }, [refresh])

    const deleteCurrConfig = () => {
        API.delete_config(currentConfig['_id']['$oid']).then(response => {
            setOpenDeleteAlert(false)
            setRefresh(true)
            setDeleted(true)
        })

    }

    if (!configs) {
        return (<div className={classes.root}>
            <Grid container justify='center' className={classes.gridContainer}>
                <Grid item>
                    <CircularProgress/>
                </Grid>
            </Grid>
        </div>)
    }

    return (
        <Container>
            <Slide direction='down' in={configs !== null} mountOnEnter unmountOnExit>
                <h1 className='title' style={{color: 'white', marginLeft: '5%'}}>Your Configurations</h1>
            </Slide>
            <ConfigTable
                configs={configs}
                createFile={(c) => createFile(c)}
                setDeleted={setDeleted}
                setCurrentConfig={setCurrentConfig}
                setOpenDeleteAlert={setOpenDeleteAlert}
                initialConfigs={initialConfigs}
                editRoute={editRoute}
            />

            <Alert
                title={'Confirm Delete Configuration'}
                content={'Are you sure you want to delete this configuration? This cannot be undone.'}
                actions={[
                    {title: 'BACK', action: (() => setOpenDeleteAlert(false))},
                    {title: 'DELETE', action: deleteCurrConfig}
                ]}
                open={openDeleteAlert}
            />

            {deleted && <DeleteConfigSnackbar title={currentConfig['title']}/>}

        </Container>
    )
}


export default ViewFrame