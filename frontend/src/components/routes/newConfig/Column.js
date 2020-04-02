import React from 'react'
import styled from 'styled-components'
import ConfigComponent from './ConfigComponent'
import {Droppable} from 'react-beautiful-dnd'
import colours from '../../../constants/colours'

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
        return this.props.configs.map((config, index) => (
            <ConfigComponent key={config.id} config={config} index={index}/>
        ))
    }
}

export default class Column extends React.Component {
    render() {
        return (
            <Container
                isHighPriority={this.props.column.title === 'High Priority'}
                isMediumPriority={this.props.column.title === 'Medium Priority'}
                isLowPriority={this.props.column.title === 'Low Priority'}
            >
                <Title>{this.props.column.title}</Title>
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
                        >
                            <InnerList configs={this.props.configs}/>
                            {provided.placeholder}
                        </ConfigList>
                    )}
                </Droppable>
            </Container>
        )
    }
}
