describe('template spec', () => {
  it('passes', () => {
    cy.visit('http://localhost:5173');
    cy.contains('路线管理').click()
    cy.contains('添加').click()
    cy.get('[placeholder="新名称"]').type('南京-北京')

    cy.contains('添加站点').click()
    cy.get('.el-select').click()
    cy.get('.el-select').should('be.visible')
    cy.contains('北京站').click()
    cy.contains('确定').click()

  })
})