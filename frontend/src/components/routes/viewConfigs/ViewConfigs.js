import React, {useState, useEffect} from 'react'
import colours from '../../../constants/colours'
import styled from 'styled-components'
import useStyles from './style'
import * as API from '../../../api'
import {
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Paper,
    Grid,
    CircularProgress,
    Tooltip, Slide
} from '@material-ui/core'
import {
    DescriptionOutlined as DowloadIcon,
    Edit as EditIcon,
    Clear as DeleteIcon
} from '@material-ui/icons'
import IconButton from '@material-ui/core/IconButton'
import Alert from './Alert'
import DeleteConfigSnackbar from './DeleteConfigSnackbar'

const Container = styled.div`
    margin: 7%;    
    display: flex;
    flex-direction: column;
`

const createFile = c => {
    API.get_checks(c['_id']['$oid']).then(response => downloadFile(response))

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
            <Slide direction="down" in={configs} mountOnEnter unmountOnExit>
                <h1 className='title' style={{color: 'white', marginLeft: '5%'}}>Your Configurations</h1>
            </Slide>
            <Slide direction="up" in={configs} mountOnEnter unmountOnExit>
                <TableContainer component={Paper} style={{margin: '5%', width: '90%'}}>
                    <Table className={classes.table} aria-label='simple table'>
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell align='right'>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {configs.map(c => (
                                <TableRow key={c['_id']}>
                                    <TableCell component='th' scope='row'>
                                        {c.title}
                                    </TableCell>
                                    <TableCell align='right' size='small'>
                                        <Tooltip title="Download Configuration File">
                                            <IconButton color='inherit' onClick={() => createFile(c)}>
                                                <DowloadIcon/>
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Edit Configuration">
                                            <IconButton color='inherit'>
                                                <EditIcon/>
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title="Delete Configuration">
                                            <IconButton
                                                color='inherit'
                                                onClick={() => {
                                                    setDeleted(false)
                                                    setCurrentConfig(c)
                                                    setOpenDeleteAlert(true)
                                                }}>
                                                <DeleteIcon/>
                                            </IconButton>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Slide>

            <Alert
                title={'Confirm Delete Configuration'}
                content={'Are you sure you want to delete this configuration? This cannot be undone.'}
                actions={[
                    {title: 'BACK', action: (() => setOpenDeleteAlert(false))},
                    {title: 'DELETE', action: deleteCurrConfig}
                ]}
                open={openDeleteAlert}
            />

            {deleted ? <DeleteConfigSnackbar/> : false}

        </Container>
    )
}


export default ViewConfigs