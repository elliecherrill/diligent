import React, {useEffect, useState} from 'react'
import {Redirect, useParams} from 'react-router-dom'
import {allConfigs, initialConfigs} from '../../../constants/config'
import {
    Button,
    CircularProgress,
    Fade,
    Grid,
    IconButton,
    Menu,
    MenuItem,
    Slide,
    TextField,
    Tooltip
} from '@material-ui/core'
import {DragDropContext} from 'react-beautiful-dnd'
import Column from '../newConfig/Column'
import * as API from '../../../api'
import styled from 'styled-components'
import routes from '../../../constants/routes'
import colours from '../../../constants/colours'
import {
    Clear as DeleteIcon,
    NoteAdd as AddDetailIcon
} from '@material-ui/icons'

const Container = styled.div`
    display: flex;
`

const EditConfig = () => {
    const id = useParams()
    const columnOrder = initialConfigs.columnOrder
    const configsList = initialConfigs.configs

    const [categories, setCategories] = useState(initialConfigs.categories)
    const [loaded, setLoaded] = useState(false)
    const [title, setTitle] = useState('')
    const [goToHome, setGoToHome] = useState(false)

    const [addCourseCode, setAddCourseCode] = useState(false)
    const [courseCode, setCourseCode] = useState('')

    const [addExerciseNum, setAddExerciseNum] = useState(false)
    const [exerciseNum, setExerciseNum] = useState('')

    const [anchorEl, setAnchorEl] = useState(null)

    useEffect(() => {
        API.get_checks(id['id']).then(r => {
            const newHigh = {
                ...categories['category-1'],
                configIds: r['high']
            }
            const newMedium = {
                ...categories['category-2'],
                configIds: r['medium']
            }

            const newLow = {
                ...categories['category-3'],
                configIds: r['low']
            }
            const newUnused = {
                ...categories['category-4'],
                configIds: allConfigs.filter(c => !r['high'].includes(c) && !r['medium'].includes(c) && !r['low'].includes(c))
            }

            const startingCategories = {
                'category-1': newHigh,
                'category-2': newMedium,
                'category-3': newLow,
                'category-4': newUnused
            }

            setCategories(startingCategories)
            setTitle(r['title'])

            if (r['courseCode'] !== null) {
                setAddCourseCode(true)
                setCourseCode(r['courseCode'])
            }

            if (r['exerciseNum'] !== null) {
                setAddExerciseNum(true)
                setExerciseNum(r['exerciseNum'])
            }
        }).then(() => setLoaded(true))

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const onDragEnd = result => {
        const {destination, source, draggableId} = result

        if (!destination) {
            return
        }

        if (destination.droppableId === source.droppableId &&
            destination.index === source.index) {
            return
        }

        const start = categories[source.droppableId]
        const finish = categories[destination.droppableId]

        if (start === finish) {
            const newConfigIds = Array.from(start.configIds)

            newConfigIds.splice(source.index, 1)
            newConfigIds.splice(destination.index, 0, draggableId)

            const newColumn = {
                ...start,
                configIds: newConfigIds
            }

            const newCategories = {
                categories,
                [newColumn.id]: newColumn
            }

            setCategories(newCategories)
            return
        }

        const startConfigIds = Array.from(start.configIds)
        startConfigIds.splice(source.index, 1)
        const newStart = {
            ...start,
            configIds: startConfigIds
        }

        const finishConfigIds = Array.from(finish.configIds)
        finishConfigIds.splice(source.index, 0, draggableId)
        const newFinish = {
            ...finish,
            configIds: finishConfigIds
        }

        const newCategories = {
            ...categories,
            [newStart.id]: newStart,
            [newFinish.id]: newFinish
        }

        setCategories(newCategories)
    }

    const getHighPriorityChecks = () => {
        return categories['category-1'].configIds
    }

    const getMediumPriorityChecks = () => {
        return categories['category-2'].configIds
    }

    const getLowPriorityChecks = () => {
        return categories['category-3'].configIds
    }

    const saveConfig = () => {
        const courseCodeValue = (addCourseCode && courseCode !== '') ? courseCode : null
        const exerciseNumValue = (addExerciseNum && exerciseNum !== '') ? exerciseNum : null

        API.delete_config(id['id']).then(() => {
            API.create_new_config(
                title,
                getHighPriorityChecks(),
                getMediumPriorityChecks(),
                getLowPriorityChecks(),
                courseCodeValue,
                exerciseNumValue
            ).then(() => {
                setGoToHome(true)
            })
        })
    }

    const handleMenuClose = () => {
        setAnchorEl(null)
    }

    const addCourseCodeClick = () => {
        setAnchorEl(null)
        setAddCourseCode(true)
    }

    const addExerciseNumClick = () => {
        setAnchorEl(null)
        setAddExerciseNum(true)
    }

    document.body.style.backgroundColor = colours.PRIMARY

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
            <Slide direction='down' in={loaded} mountOnEnter unmountOnExit>
                <div style={{display: 'flex', flexDirection: 'row'}}>
                    <h1 className='title' style={{color: 'white', marginLeft: '5%'}}>
                        {title}
                    </h1>
                    <div style={{marginLeft: '1%', marginTop: '0.75%'}}>
                        <Tooltip title='Add More Details'>
                            <IconButton
                                color='secondary'
                                disabled={addExerciseNum && addCourseCode}
                                onClick={(e) => setAnchorEl(e.currentTarget)}
                            >
                                <AddDetailIcon/>
                            </IconButton>
                        </Tooltip>
                        {(!addExerciseNum || !addCourseCode) &&
                        <Menu
                            id='simple-menu'
                            anchorEl={anchorEl}
                            keepMounted
                            open={Boolean(anchorEl)}
                            onClose={handleMenuClose}
                        >
                            {!addCourseCode && <MenuItem onClick={addCourseCodeClick}>Add Course Code</MenuItem>}
                            {!addExerciseNum && <MenuItem onClick={addExerciseNumClick}>Add Exercise Number</MenuItem>}
                        </Menu>
                        }
                    </div>
                </div>
            </Slide>
            <Slide direction='up' in={loaded} mountOnEnter unmountOnExit>
                <div style={{paddingLeft: '5%', paddingRight: '5%'}}>
                    <DragDropContext
                        onDragEnd={onDragEnd}
                    >
                        <Container>
                            {columnOrder.map((columnId) => {
                                const column = categories[columnId]
                                const configs = column.configIds.map(configId => configsList[configId])

                                return <Column key={column.id} column={column} configs={configs}/>
                            })}
                        </Container>
                    </DragDropContext>
                </div>
            </Slide>
            {/*Simple Button*/}
            <Slide direction='up' in={!addCourseCode && !addExerciseNum} mountOnEnter unmountOnExit>
                <div style={{display: 'flex', justifyContent: 'center', marginTop: '5%'}}>
                    <Button
                        variant='outlined'
                        color='secondary'
                        onClick={() => saveConfig()}
                    >
                        Save Changes
                    </Button>
                </div>
            </Slide>

            {/*More Details*/}
            <Slide direction='up' in={addCourseCode || addExerciseNum} mountOnEnter unmountOnExit>
                <div style={{backgroundColor: 'white', padding: '3%', marginTop: '5%'}}>
                    <div style={{display: 'flex', flexDirection: 'column'}}>
                        {(addCourseCode || addExerciseNum) &&
                        <div style={{
                            display: 'flex',
                            flexDirection: 'row',
                            justifyContent: 'center',
                            marginTop: '2%'
                        }}>
                            <Fade in={addCourseCode}>
                                <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center'}}>
                                    <TextField
                                        label='Course Code'
                                        variant='outlined'
                                        color='primary'
                                        autoComplete='off'
                                        style={{marginLeft: '1%', marginRight: '1%'}}
                                        id={'course-code-input'}
                                        helperText={'For Example: CO161'}
                                        onChange={(e) => setCourseCode(e.target.value)}
                                        value={courseCode}
                                    />
                                    <div>
                                        <IconButton
                                            color='primary'
                                            onClick={() => setAddCourseCode(false)}
                                        >
                                            <DeleteIcon/>
                                        </IconButton>
                                    </div>
                                </div>
                            </Fade>
                            <Fade in={addExerciseNum}>
                                <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center'}}>
                                    <TextField
                                        label='Exercise Number'
                                        variant='outlined'
                                        color='primary'
                                        autoComplete='off'
                                        style={{marginLeft: '1%', marginRight: '1%'}}
                                        id={'ex-num-input'}
                                        helperText={'For Example: 17 Java Spreadsheet'}
                                        onChange={(e) => setExerciseNum(e.target.value)}
                                        value={exerciseNum}
                                    />
                                    <div>
                                        <IconButton
                                            color='primary'
                                            onClick={() => setAddExerciseNum(false)}
                                        >
                                            <DeleteIcon/>
                                        </IconButton>
                                    </div>
                                </div>
                            </Fade>
                        </div>
                        }

                        <div style={{display: 'flex', justifyContent: 'center', marginTop: '1%'}}>
                            <Button
                                variant='contained'
                                color='primary'
                                onClick={() => saveConfig()}
                            >
                                Save Changes
                            </Button>
                        </div>
                    </div>
                </div>
            </Slide>

            {goToHome &&
            <Redirect push to={{
                pathname: routes.HOME,
                state: {title: title, new: false, edit: true}
            }}/>}
        </div>
    )
}

export default EditConfig



