import React from 'react'
import styled from 'styled-components'
import ConfigComponent from './ConfigComponent'
import {Droppable} from 'react-beautiful-dnd'
import colours from '../../../constants/colours'
import {Info as InfoIcon} from '@material-ui/icons'
import {Tooltip} from '@material-ui/core'

const Container = styled.div`
    margin: 8px;
    border: 5px solid ${props => (props.isHighPriority ? colours.RED : (props.isMediumPriority ? colours.AMBER : (props.isLowPriority ? colours.GREEN : 'lightgray')))};
    border-radius: 10px;
    width: 100%;
    
    display: flex;
    flex-direction: column;
    
    background-color: white;
`
const Title = styled.h3`
    padding: 8px;
    text-align: center;
`
const ConfigList = styled.div`
    padding: 8px;
    background-color: ${props => (!props.isDraggingOver ? 'white' : (props.isHighPriority ? colours.RED : (props.isMediumPriority ? colours.AMBER : (props.isLowPriority ? colours.GREEN : 'white'))))};
    transition: background-color 0.2s ease;
    flex-grow: 1;
    min-height: 100px;
    border-radius: 2px;
`

class InnerList extends React.Component {
    shouldComponentUpdate(nextProps) {
        return nextProps.configs !== this.props.configs
    }

    render() {
        return this.props.configs.map((config, index) => {
            const searchResult = this.props.searching ? config.content.toLowerCase().includes(this.props.searchText) : false
            return <ConfigComponent key={config.id} config={config} index={index} searchResult={searchResult}/>
        })
    }
}

export default class Column extends React.Component {
    getInfo = (title) => {
        if (title === 'High Priority') {
            return 'TODO: Explain what high priority means'
        }

        if (title === 'Medium Priority') {
            return 'TODO: Explain what medium priority means'
        }

        if (title === 'Low Priority') {
            return 'TODO: Explain what low priority means'
        }

        return 'TODO: Explain what not in use means'
    }

    render() {
        return (
            <Container
                isHighPriority={this.props.column.title === 'High Priority'}
                isMediumPriority={this.props.column.title === 'Medium Priority'}
                isLowPriority={this.props.column.title === 'Low Priority'}
            >
                <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                    <Title>{this.props.column.title}</Title>
                    <Tooltip title={this.getInfo(this.props.column.title)}>
                        <InfoIcon color='primary'/>
                    </Tooltip>
                </div>
                <Droppable droppableId={this.props.column.id}>
                    {(provided, snapshot) => (
                        <ConfigList
                            innerRef={provided.innerRef}
                            ref={provided.innerRef}
                            {...provided.droppableProps}
                            isDraggingOver={snapshot.isDraggingOver}
                            isHighPriority={this.props.column.title === 'High Priority'}
                            isMediumPriority={this.props.column.title === 'Medium Priority'}
                            isLowPriority={this.props.column.title === 'Low Priority'}
                            style={{maxHeight: '40vh', overflow: 'auto'}}
                        >
                            <InnerList configs={this.props.configs} searching={this.props.searching} searchText={this.props.searchText}/>
                            {provided.placeholder}
                        </ConfigList>
                    )}
                </Droppable>
            </Container>
        )
    }
}
