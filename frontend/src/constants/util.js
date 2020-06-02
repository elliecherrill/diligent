export const downloadFile = async (response, filename) => {
    const blob = new Blob([JSON.stringify(response)], {type: 'application/json'})
    const href = await URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = href
    link.download = filename + '.json'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}