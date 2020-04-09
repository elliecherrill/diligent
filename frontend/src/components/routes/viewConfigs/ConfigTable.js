import React from 'react'
import {
    Paper,
    Slide,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip
} from '@material-ui/core'
import IconButton from '@material-ui/core/IconButton'
import {
    Clear as DeleteIcon,
    DescriptionOutlined as DowloadIcon,
    Edit as EditIcon
} from '@material-ui/icons'
import useStyles from './style'

const ConfigTable = ({configs, createFile, setDeleted, setCurrentConfig, setOpenDeleteAlert}) => {
    const classes = useStyles()

    return (
        <Slide direction="up" in={configs !== null} mountOnEnter unmountOnExit>
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
    )
}

export default ConfigTable