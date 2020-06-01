import React from 'react'
import {
    Divider,
    IconButton,
    InputBase,
    Paper,
} from '@material-ui/core'
import {
    Clear as ClearIcon,
    Search as SearchIcon
} from '@material-ui/icons'

const SearchBar = ({searchBy, updateSearchText, searching, searchResults, stopSearching, moveToTop}) => {
    const ENTER = 13

    return (
        <Paper style={{display: 'flex', margin: '0.5%'}}>
            <InputBase
                style={{marginLeft: '1%', flex: '1'}}
                placeholder='Search'
                value={searchBy}
                onChange={(e) => updateSearchText(e)}
                onKeyDown={(e) => {
                    if (e.keyCode === ENTER) {
                        moveToTop()
                    }
                }}
            />
            {searching &&
            <Divider orientation='vertical' flexItem style={{margin: '0.5%'}}/>
            }
            {searching &&
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                color: 'gray',
                marginLeft: '0.5%'
            }}>
                <p>{searchResults} search results</p>
            </div>
            }
            {searching ?
                <IconButton
                    style={{padding: '1%'}}
                    onClick={stopSearching}
                >
                    <ClearIcon/>
                </IconButton>
                :
                <IconButton
                    style={{padding: '1%'}}
                    onClick={moveToTop}
                >
                    <SearchIcon/>
                </IconButton>
            }
        </Paper>
    )
}

export default SearchBar