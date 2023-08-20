describe('template spec', () => {
  it('passes', () => {
    cy.visit('http://localhost:5173')
    cy.contains('登录').click();
    cy.get('[placeholder="用户名"]').type('prk123456')
    cy.get('[placeholder="密码"]').type('Prk123456')
    cy.contains('登录').click();
  })

})