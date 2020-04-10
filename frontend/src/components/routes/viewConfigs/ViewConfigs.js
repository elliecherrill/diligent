import React, {useState, useEffect} from 'react'
import colours from '../../../constants/colours'
import styled from 'styled-components'
import useStyles from './style'
import * as API from '../../../api'
import {
    Grid,
    CircularProgress,
    Slide
} from '@material-ui/core'
import Alert from './Alert'
import DeleteConfigSnackbar from './DeleteConfigSnackbar'
import ConfigTable from './ConfigTable'

const Container = styled.div`
    margin: 7%;    
    display: flex;
    flex-direction: column;
`

const ViewConfigs = () => {
    document.body.style.backgroundColor = colours.PRIMARY

    const classes = useStyles()
    const [configs, setConfigs] = useState(null)
    const [openDeleteAlert, setOpenDeleteAlert] = useState(false)
    const [currentConfig, setCurrentConfig] = useState(null)
    const [refresh, setRefresh] = useState(false)
    const [deleted, setDeleted] = useState(false)

    useEffect(() => {
        API.get_my_configs().then(r => {
            setConfigs(r)
            setRefresh(false)
        })
    }, [refresh])

    const createFile = c => {
        API.get_checks_for_download(c['_id']['$oid']).then(response => downloadFile(response))

    }

    const downloadFile = async (response) => {
        const blob = new Blob([JSON.stringify(response)], {type: 'application/json'})
        const href = await URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = href
        link.download = 'diligent.json'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    }

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
            <Slide direction="down" in={configs !== null} mountOnEnter unmountOnExit>
                <h1 className='title' style={{color: 'white', marginLeft: '5%'}}>Your Configurations</h1>
            </Slide>
            <ConfigTable
                configs={configs}
                createFile={(c) => createFile(c)}
                setDeleted={setDeleted}
                setCurrentConfig={setCurrentConfig}
                setOpenDeleteAlert={setOpenDeleteAlert}
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


export default ViewConfigs