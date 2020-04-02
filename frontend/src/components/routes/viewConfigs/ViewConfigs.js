import React, {useState, useEffect} from 'react'
import colours from '../../../constants/colours'
import styled from 'styled-components'
import useStyles from './style'
import * as API from '../../../api'
import {TableContainer, Table, TableHead, TableRow, TableCell, TableBody, Paper, Grid, CircularProgress} from '@material-ui/core'
import {
    DescriptionOutlined as DowloadIcon
} from '@material-ui/icons'
import IconButton from '@material-ui/core/IconButton';

const Container = styled.div`
    margin: 10%;
    border: 3px solid ${colours.PRIMARY};
    border-radius: 5px;
    background-color: 'white';
    
    display: flex;
    flex-direction: column;
`

const createFile = c => {
    console.log(c)
}

const ViewConfigs = () => {
    document.body.style.backgroundColor = 'inherit'
    const classes = useStyles()
    const [configs, setConfigs] = useState(null)

    useEffect(() => {
        API.get_my_configs().then(r => setConfigs(r))
    }, [])

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
            <h1 className='title' style={{color: colours.PRIMARY, marginLeft: '5%'}}>Your Configurations</h1>
            <TableContainer component={Paper} style={{margin: '5%', width: '90%'}}>
                <Table className={classes.table} aria-label='simple table'>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell align='right'>Download</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {configs.map(c => (
                            <TableRow key={c['_id']}>
                                <TableCell component='th' scope='row'>
                                    {c.title}
                                </TableCell>
                                <TableCell align='right'>
                                    <IconButton color='inherit' onClick={() => createFile(c)}>
                                        <DowloadIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    )
}


export default ViewConfigs