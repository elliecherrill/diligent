import React, {useEffect, useState} from 'react'
import {Redirect, useParams} from 'react-router-dom'
import {allConfigs, initialConfigs} from '../../../constants/config'
import {Button, CircularProgress, Grid, Slide} from '@material-ui/core'
import {DragDropContext} from 'react-beautiful-dnd'
import Column from '../newConfig/Column'
import * as API from '../../../api'
import styled from 'styled-components'
import routes from '../../../constants/routes'
import colours from '../../../constants/colours'

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
                <h1 className='title' style={{color: 'white', marginLeft: '5%'}}>{title}</h1>
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
            <Slide direction='up' in={true} mountOnEnter unmountOnExit>
                <div style={{width: '100%', marginTop: '5%'}}>
                    <div style={{display: 'flex', justifyContent: 'center'}}>
                        <Button
                            variant='outlined'
                            color='secondary'
                            onClick={() => {
                                API.delete_config(id['id']).then(() => {
                                    API.create_new_config(title, getHighPriorityChecks(), getMediumPriorityChecks(), getLowPriorityChecks()).then(() => {
                                        setGoToHome(true)
                                    })
                                })
                            }}
                        >
                            SAVE CHANGES
                        </Button>

                        {goToHome &&
                        <Redirect to={{pathname: routes.HOME, state: {title: title, new: false, edit: true}}}/>}
                    </div>
                </div>
            </Slide>
        </div>
    )
}

export default EditConfig



