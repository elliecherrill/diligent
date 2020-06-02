import React, {useState} from 'react'
import {
    Popover,
    Paper,
    Slide,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tooltip,
    Typography
} from '@material-ui/core'
import IconButton from '@material-ui/core/IconButton'
import {
    Clear as DeleteIcon,
    Description as DowloadIcon,
    Edit as EditIcon,
    Visibility as ViewIcon,
} from '@material-ui/icons'
import useStyles from './style'
import routes from '../../../constants/routes'
import {Redirect} from 'react-router-dom'
import * as API from '../../../api'

const getInitialAnchors = (configs) => {
    return configs.map(c => null)
}

const ConfigTable = ({configs, createFile, setDeleted, setCurrentConfig, setOpenDeleteAlert, initialConfigs, editRoute}) => {
    const classes = useStyles()
    const [editConfig, setEditConfig] = useState(null)
    const [anchorEl, setAnchorEl] = useState(getInitialAnchors(configs))

    const [highChecks, setHighChecks] = useState([])
    const [mediumChecks, setMediumChecks] = useState([])
    const [lowChecks, setLowChecks] = useState([])

    const [mostRecentConfig, setMostRecentConfig] = useState(null)

    const [refresh, setRefresh] = useState(true)
    const [loaded, setLoaded] = useState(true)

    const handlePopoverClose = index => {
        const newAnchors = anchorEl
        newAnchors[index] = null
        setAnchorEl(newAnchors)

        setRefresh(!refresh)
    }

    const format = list => {
        if (list.length === 0) {
            return 'None'
        }

        let formattedString = ''

        for (let i = 0; i < list.length - 1; i++) {
            formattedString += list[i] + ', '
        }

        return formattedString + list[list.length - 1]
    }

    const handleViewClick = (element, index) => {
        if (mostRecentConfig !== configs[index]['_id']['$oid']) {
            setLoaded(false)
            API.get_checks(configs[index]['_id']['$oid']).then(r => {
                setHighChecks(format(r['high'].map(c => initialConfigs.configs[c].content)))
                setMediumChecks(format(r['medium'].map(c => initialConfigs.configs[c].content)))
                setLowChecks(format(r['low'].map(c => initialConfigs.configs[c].content)))

                setMostRecentConfig(configs[index]['_id']['$oid'])
            }).then(() => setLoaded(true))
        }

        const newAnchors = anchorEl
        newAnchors[index] = element.currentTarget
        setAnchorEl(newAnchors)

        setRefresh(!refresh)
    }

    return (
        <div>
            <Slide direction='up' in={configs !== null} mountOnEnter unmountOnExit>
                <TableContainer component={Paper} style={{margin: '5%', width: '90%'}}>
                    <Table className={classes.table} aria-label='simple table'>
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Course Code</TableCell>
                                <TableCell>Exercise Number</TableCell>
                                <TableCell align='right'>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {configs.map((c, index) => (
                                <TableRow key={c['_id']['$oid']}>
                                    <TableCell component='th' scope='row'>
                                        {c.title}
                                    </TableCell>
                                    <TableCell component='th' scope='row'>
                                        {c.courseCode}
                                    </TableCell>
                                    <TableCell component='th' scope='row'>
                                        {c.exerciseNum}
                                    </TableCell>
                                    <TableCell align='right' size='small'>
                                        <Tooltip title='Quick View Configuration'>
                                            <IconButton
                                                onClick={(e) => handleViewClick(e, index)}
                                                color='inherit'
                                            >
                                                <ViewIcon/>
                                            </IconButton>
                                        </Tooltip>
                                        <Popover
                                            open={anchorEl[index] !== null && loaded}
                                            anchorEl={anchorEl[index]}
                                            onClose={() => handlePopoverClose(index)}
                                            anchorOrigin={{
                                                vertical: 'top',
                                                horizontal: 'left',
                                            }}
                                            transformOrigin={{
                                                vertical: 'top',
                                                horizontal: 'right',
                                            }}
                                        >
                                            <Typography className={classes.typography}>
                                                High Priority Checks: {highChecks} <br />
                                                Medium Priority Checks: {mediumChecks} <br />
                                                Low Priority Checks: {lowChecks}
                                            </Typography>
                                        </Popover>
                                        <Tooltip title='Download Configuration File'>
                                            <IconButton color='inherit' onClick={() => createFile(c)}>
                                                <DowloadIcon/>
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title='Edit Configuration'>
                                            <IconButton color='inherit' onClick={() => setEditConfig(c)}>
                                                <EditIcon/>
                                            </IconButton>
                                        </Tooltip>
                                        <Tooltip title='Delete Configuration'>
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

            {editConfig !== null && <Redirect push to={editRoute + '/' + editConfig['_id']['$oid']}/>}

        </div>
    )
}

export default ConfigTable